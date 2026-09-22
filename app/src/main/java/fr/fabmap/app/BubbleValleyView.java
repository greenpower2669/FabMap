package fr.fabmap.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/** Finite 2D projection of real graph nodes, never an infinite/generated map. */
final class BubbleValleyView extends View {
    interface Listener {
        void open(String id);
        void preview(String title,String explanation);
    }
    private static final int CAP=160;
    private static final int[] COLORS={0xff3C72B2,0xff298B75,0xff9564AE,0xffAD7142,0xffAD5A7C,0xff428C9B,0xff898D42};
    private static final class Bubble {
        final String id,title,detail,icon;final int depth,color;final float r;
        float x,y;
        Bubble(String id,String title,String detail,String icon,int depth,int color,float x,float y) {
            this.id=id;this.title=title;this.detail=detail;this.icon=icon;this.depth=depth;this.color=color;
            this.x=x;this.y=y;this.r=depth==0?86:72;
        }
    }
    private static final class Edge {
        final Bubble a,b;
        Edge(Bubble a,Bubble b){this.a=a;this.b=b;}
    }
    private final ArrayList<Bubble> all=new ArrayList<>();
    private final HashMap<String,Bubble> indexed=new HashMap<>();
    private final ArrayList<Edge> edges=new ArrayList<>();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Listener listener;
    private final IconAssets iconAssets;
    private final ScaleGestureDetector pinch;
    private final float density,slop;
    private float minX=-130,maxX=130,minY=-130,maxY=130;
    private float scale=1f,panX,panY,downX,downY,lastX,lastY;
    private Bubble down;
    private boolean moved,longPressed,pinching,limitReached;

    BubbleValleyView(Context context,JSONObject nodes,String root,IconAssets icons,Listener listener) {
        super(context);this.listener=listener;this.iconAssets=icons;
        density=getResources().getDisplayMetrics().density;
        slop=ViewConfiguration.get(context).getScaledTouchSlop();
        build(nodes,root);
        pinch=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d) {
                float previous=scale;
                scale=Math.max(.35f,Math.min(3f,scale*d.getScaleFactor()));
                float ratio=scale/previous;
                float fx=d.getFocusX()-getWidth()/2f,fy=d.getFocusY()-getHeight()/2f;
                panX=fx-(fx-panX)*ratio;
                panY=fy-(fy-panY)*ratio;
                constrain();invalidate();return true;
            }
        });
        setFocusable(true);
        setContentDescription("Vallée des bulles. Glisser pour déplacer, pincer pour zoomer. Toucher une bulle pour ouvrir ; appui long pour écouter. Mode guidé disponible.");
    }
    private float px(float world){return world*density*scale;}
    private float sx(float x){return getWidth()/2f+panX+px(x);}
    private float sy(float y){return getHeight()/2f+panY+px(y);}
    private static double angle(String id){
        return (id.hashCode()&0xffffffffL)/(double)0x100000000L*Math.PI*2d;
    }
    private void build(JSONObject graph,String rootId) {
        JSONObject first=graph.optJSONObject(rootId);
        if(first==null)return;
        Bubble center=new Bubble(rootId,first.optString("title","Bulle"),first.optString("description",""),first.optString("icon",""),0,0xff315D97,0,0);
        all.add(center);indexed.put(rootId,center);
        ArrayDeque<Bubble> queue=new ArrayDeque<>();queue.add(center);
        while(!queue.isEmpty()&&all.size()<CAP) {
            Bubble parent=queue.removeFirst();
            JSONObject value=graph.optJSONObject(parent.id);
            JSONArray children=value==null?null:value.optJSONArray("children");
            if(children==null)continue;
            for(int i=0;i<children.length();i++){
                String id=children.optString(i);
                JSONObject child=graph.optJSONObject(id);
                if(child==null||indexed.containsKey(id))continue;
                int depth=parent.depth+1;
                int color=parent.depth==0?COLORS[Math.floorMod(id.hashCode(),COLORS.length)]:parent.color;
                double a=angle(id);
                float distance=parent.depth==0?330:180;
                float startX=parent.x+(float)Math.cos(a)*distance;
                float startY=parent.y+(float)Math.sin(a)*distance;
                Bubble next=new Bubble(id,child.optString("title","Bulle"),child.optString("description",""),child.optString("icon",""),depth,color,startX,startY);
                // Reposition only the incoming bubble; older positions remain unchanged.
                for(int attempt=0;attempt<90;attempt++){
                    boolean collision=false;
                    for(Bubble previous:all){
                        float dx=next.x-previous.x,dy=next.y-previous.y;
                        float minimum=next.r+previous.r+26;
                        if(dx*dx+dy*dy<minimum*minimum){collision=true;break;}
                    }
                    if(!collision)break;
                    double spin=a+2.3999632*attempt;
                    float radius=27f*(float)Math.sqrt(attempt+1);
                    next.x=startX+(float)Math.cos(spin)*radius;
                    next.y=startY+(float)Math.sin(spin)*radius;
                }
                all.add(next);indexed.put(id,next);queue.add(next);
                if(all.size()>=CAP){limitReached=true;break;}
            }
        }
        for(Bubble b:all){
            JSONObject node=graph.optJSONObject(b.id);
            JSONArray children=node==null?null:node.optJSONArray("children");
            if(children!=null)for(int i=0;i<children.length();i++){
                Bubble child=indexed.get(children.optString(i));
                if(child!=null)edges.add(new Edge(b,child));
            }
            minX=Math.min(minX,b.x-b.r-30);maxX=Math.max(maxX,b.x+b.r+30);
            minY=Math.min(minY,b.y-b.r-30);maxY=Math.max(maxY,b.y+b.r+30);
        }
    }
    @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){
        super.onSizeChanged(w,h,oldW,oldH);
        if(oldW==0||oldH==0)reset();else constrain();
    }
    void reset(){
        if(getWidth()==0||getHeight()==0)return;
        float fit=Math.min(getWidth()/(density*(maxX-minX)),getHeight()/(density*(maxY-minY)));
        scale=Math.max(.42f,Math.min(1f,fit*.9f));
        panX=panY=0;constrain();invalidate();
    }
    void zoomBy(float amount){scale=Math.max(.35f,Math.min(3f,scale*amount));constrain();invalidate();}
    private void constrain(){
        if(getWidth()==0||getHeight()==0)return;
        float margin=28*density,cx=getWidth()/2f,cy=getHeight()/2f;
        if(px(maxX-minX)<getWidth()-2*margin)panX=-px((minX+maxX)/2f);
        else panX=Math.max(getWidth()-margin-cx-px(maxX),Math.min(margin-cx-px(minX),panX));
        if(px(maxY-minY)<getHeight()-2*margin)panY=-px((minY+maxY)/2f);
        else panY=Math.max(getHeight()-margin-cy-px(maxY),Math.min(margin-cy-px(minY),panY));
    }
    private boolean visible(Bubble b){
        float x=sx(b.x),y=sy(b.y),r=px(b.r+40);
        return x+r>=0&&x-r<=getWidth()&&y+r>=0&&y-r<=getHeight();
    }
    private Bubble hit(float x,float y){
        for(int i=all.size()-1;i>=0;i--){
            Bubble b=all.get(i);
            float dx=x-sx(b.x),dy=y-sy(b.y),r=Math.max(px(b.r),56*density);
            if(dx*dx+dy*dy<=r*r)return b;
        }
        return null;
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        c.drawColor(0xffF7FAFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(density,2*density*scale));
        for(Edge edge:edges){
            if(!visible(edge.a)&&!visible(edge.b))continue;
            paint.setColor((edge.a.color&0xffffff)|0x44000000);
            c.drawLine(sx(edge.a.x),sy(edge.a.y),sx(edge.b.x),sy(edge.b.y),paint);
        }
        paint.setStyle(Paint.Style.FILL);
        for(Bubble b:all){
            if(!visible(b))continue;
            float x=sx(b.x),y=sy(b.y),r=px(b.r);
            paint.setColor((b.color&0xffffff)|0x20000000);c.drawCircle(x,y,r+px(26),paint);
            paint.setColor((b.color&0xffffff)|0x50000000);c.drawCircle(x,y,r+px(11),paint);
            paint.setColor(b.color);c.drawCircle(x,y,r,paint);
            if(scale<.43f)continue;
            Bitmap itemIcon=iconAssets.get(b.icon);
            if(itemIcon!=null){
                float centerY=y-r*.43f,side=r*.80f;
                // Transparent glass backing, not a white image rectangle.
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(0x42FFFFFF);
                c.drawCircle(x,centerY,r*.49f,paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(density*1.1f);
                paint.setColor(0x88FFFFFF);
                c.drawCircle(x,centerY,r*.49f,paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                c.drawBitmap(itemIcon,null,new android.graphics.RectF(
                    x-side/2f,centerY-side/2f,x+side/2f,centerY+side/2f),paint);
            }
            // Subtle two-tone lettering + soft shadow; never an opaque banner.
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(density*Math.max(11,Math.min(19,16*scale)));
            paint.setTextAlign(Paint.Align.CENTER);
            String name=b.title.trim();if(name.isEmpty())name="Bulle";
            String[] parts=name.split("\\s+");
            StringBuilder first=new StringBuilder(),second=new StringBuilder();
            for(String part:parts){
                String joined=first.length()==0?part:first+" "+part;
                if(first.length()==0||paint.measureText(joined)<r*1.65f)first=new StringBuilder(joined);
                else{if(second.length()>0)second.append(' ');second.append(part);}
            }
            String a=truncate(first.toString(),r*1.65f);
            String d=truncate(second.toString(),r*1.65f);
            paint.setShader(new LinearGradient(x-r*.88f,0,x+r*.88f,0,
                0xffFFFFFF,0xffC3E8F8,Shader.TileMode.CLAMP));
            paint.setShadowLayer(density*2.6f,0,density*1.3f,0xd010293e);
            if(d.isEmpty()){
                c.drawText(a,x,itemIcon==null?y+r*.18f:y+r*.50f,paint);
            }else{
                float firstY=itemIcon==null?y-r*.03f:y+r*.32f;
                c.drawText(a,x,firstY,paint);
                c.drawText(d,x,firstY+r*.28f,paint);
            }
            paint.clearShadowLayer();
            paint.setShader(null);
        }
        if(limitReached){
            paint.setColor(0xff234568);paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(12*density);
            c.drawText("160 bulles affichées · le mode guidé reste complet",7*density,getHeight()-11*density,paint);
        }
    }
    private String truncate(String s,float max){
        if(paint.measureText(s)<=max)return s;
        while(s.length()>1&&paint.measureText(s+"…")>max)s=s.substring(0,s.length()-1);
        return s+"…";
    }
    private final Runnable hold=()->{
        if(down!=null&&!moved&&!pinching){
            longPressed=true;performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            listener.preview(down.title,down.detail);
        }
    };
    private void cancelHold(){handler.removeCallbacks(hold);}
    @Override public boolean onTouchEvent(MotionEvent event){
        pinch.onTouchEvent(event);
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                downX=lastX=event.getX();downY=lastY=event.getY();
                moved=longPressed=pinching=false;
                down=hit(downX,downY);
                if(down!=null)handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                pinching=moved=true;cancelHold();return true;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount()>1){pinching=moved=true;cancelHold();return true;}
                float x=event.getX(),y=event.getY();
                if(Math.hypot(x-downX,y-downY)>slop){moved=true;cancelHold();}
                if(moved&&!longPressed){
                    panX+=x-lastX;panY+=y-lastY;constrain();invalidate();
                }
                lastX=x;lastY=y;return true;
            case MotionEvent.ACTION_POINTER_UP:
                moved=true;cancelHold();return true;
            case MotionEvent.ACTION_UP:
                cancelHold();
                if(!moved&&!longPressed&&down!=null){performClick();listener.open(down.id);}
                down=null;return true;
            case MotionEvent.ACTION_CANCEL:
                cancelHold();down=null;return true;
            default:return true;
        }
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override protected void onDetachedFromWindow(){cancelHold();super.onDetachedFromWindow();}
}
