package fr.fabmap.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Accessible color swatch drawn as a hexagonal button. */
final class HexColorTile extends View {
    private final int color;
    private final String name;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean selected;
    HexColorTile(Context context,String name,int color){
        super(context);this.name=name;this.color=color;
        setFocusable(true);setClickable(true);
        setContentDescription("Couleur "+name+", "+String.format(java.util.Locale.ROOT,"#%06X",color&0xffffff));
    }
    void selected(boolean value){selected=value;invalidate();}
    @Override protected void onMeasure(int w,int h){
        float density=getResources().getDisplayMetrics().density;
        int size=(int)(80*density+.5f);
        setMeasuredDimension(resolveSize(size,w),resolveSize(size,h));
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float cx=getWidth()/2f,cy=getHeight()/2f;
        float r=Math.min(getWidth(),getHeight())*.39f;
        Path path=new Path();
        for(int i=0;i<6;i++){
            double a=Math.PI/3*i-Math.PI/6;
            float x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r;
            if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
        }
        path.close();
        paint.setStyle(Paint.Style.FILL);paint.setColor(color);
        paint.setShadowLayer(7,0,3,0x66000000);
        c.drawPath(path,paint);paint.clearShadowLayer();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(selected?0xff132E4C:0x66FFFFFF);
        paint.setStrokeWidth(selected?5:2);c.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((Color.red(color)*299+Color.green(color)*587+Color.blue(color)*114)/1000>150?0xff172E44:Color.WHITE);
        paint.setTextSize(getResources().getDisplayMetrics().scaledDensity*12);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(name,cx,cy+paint.getTextSize()*.3f,paint);
    }
    @Override public boolean performClick(){super.performClick();return true;}
}
