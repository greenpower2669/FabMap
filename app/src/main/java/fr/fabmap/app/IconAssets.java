package fr.fabmap.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.widget.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

/** Lightweight identifiers: built:01-00, custom:UUID.png or legacy custom:UUID.jpg. */
final class IconAssets {
    static final String[][] GROUPS={
        {"Thèmes","Maison","Grande pièce","Télévision","Télécommande","Cuisine","Santé","Famille","Musique"},
        {"Actions","Écouter","Rechercher","Sauvegarder","Télécharger","Importer","Modifier","Photo","Recentrer"},
        {"États","Validé","Favori","Important","À écouter","Partagée","Sous-bulles","En cours","Terminé"},
        {"Navigation","Retour","Accueil","Zoomer","Dézoomer","Explorer","Déplacer","Ouvrir","Fermer"},
        {"Procédure","Étape","Choix","Attendre","Appuyer","Allumer","HDMI3","Télécommande TV","C'est fait"},
        {"Bulles","Bulle simple","Bulle parent","Bulle fille","Bulle liée","Bulle photo","Bulle procédure","Bulle parlante","Bulle favorite"},
        {"Repérage","Ici","À gauche","À droite","En haut","En bas","À côté","Devant","Derrière"}
    };
    static final class Option {
        final String label, id;
        Option(String label,String id){this.label=label;this.id=id;}
    }
    private final Context context;
    private final File media;
    private final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(3*1024*1024){
        @Override protected int sizeOf(String key,Bitmap value){return value.getByteCount();}
    };
    IconAssets(Context context,File media){this.context=context;this.media=media;}
    static boolean customFile(String value){return value!=null&&value.matches("custom:[a-zA-Z0-9_-]+\\.(png|jpg)");}
    static boolean builtFile(String value){return value!=null&&value.matches("built:0[1-7]-0[0-7]");}
    private static String file(String icon){return icon.substring(7);}
    Bitmap get(String icon){
        if(!customFile(icon)&&!builtFile(icon))return null;
        Bitmap cached=cache.get(icon);if(cached!=null)return cached;
        BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=2;
        Bitmap bitmap=null;
        try{
            if(customFile(icon))bitmap=BitmapFactory.decodeFile(new File(media,file(icon)).getAbsolutePath(),opt);
            else try(InputStream in=context.getAssets().open("icons/default/"+icon.substring(6)+".png")){
                bitmap=BitmapFactory.decodeStream(in,null,opt);
            }
        }catch(IOException ignored){}
        if(bitmap!=null)cache.put(icon,bitmap);
        return bitmap;
    }
    ImageView view(String icon,int dp){
        Bitmap bm=get(icon);if(bm==null)return null;
        ImageView image=new ImageView(context);
        int size=(int)(dp*context.getResources().getDisplayMetrics().density+.5f);
        image.setImageBitmap(bm);image.setAdjustViewBounds(true);
        image.setMaxWidth(size);image.setMaxHeight(size);
        image.setLayoutParams(new android.widget.LinearLayout.LayoutParams(size,size));
        image.setContentDescription("Icône");
        return image;
    }
    Drawable drawable(String icon,int dp){
        Bitmap bm=get(icon);if(bm==null)return null;
        BitmapDrawable drawable=new BitmapDrawable(context.getResources(),bm);
        int size=(int)(dp*context.getResources().getDisplayMetrics().density+.5f);
        drawable.setBounds(0,0,size,size);return drawable;
    }
    ArrayList<Option> options(){
        ArrayList<Option> choices=new ArrayList<>();
        for(int group=0;group<GROUPS.length;group++){
            for(int index=0;index<8;index++){
                String id=String.format(java.util.Locale.ROOT,"built:%02d-%02d",group+1,index);
                if(get(id)!=null)choices.add(new Option(GROUPS[group][0]+" · "+GROUPS[group][index+1],id));
            }
        }
        return choices;
    }
    static boolean installCatalog(JSONObject nodes){
        JSONObject home=nodes.optJSONObject("home");if(home==null)return false;
        String catalog="fabmap-icons";
        if(nodes.optJSONObject(catalog)!=null)return false;
        JSONObject top=new JSONObject();put(top,"id",catalog);put(top,"title","Icônes FabMap");
        put(top,"description","Catalogue des bulles conceptuelles liées aux planches.");
        JSONArray families=new JSONArray();put(top,"children",families);put(top,"steps",new JSONArray());
        put(nodes,catalog,top);
        JSONArray homeLinks=home.optJSONArray("children");
        if(homeLinks==null){homeLinks=new JSONArray();put(home,"children",homeLinks);}
        homeLinks.put(catalog);
        // The seven concept families are real nodes; missing future boards show text until Fab uploads them.
        for(int group=0;group<GROUPS.length;group++){
            String familyId=String.format(java.util.Locale.ROOT,"fabmap-family-%02d",group+1);
            JSONObject category=new JSONObject();
            JSONArray children=new JSONArray();
            put(category,"id",familyId);put(category,"title",GROUPS[group][0]);
            put(category,"description","Huit icônes et leurs bulles.");put(category,"children",children);
            put(category,"steps",new JSONArray());
            put(nodes,familyId,category);families.put(familyId);
            for(int i=0;i<8;i++){
                String id=String.format(java.util.Locale.ROOT,"fabmap-icon-%02d-%02d",group+1,i);
                String key=String.format(java.util.Locale.ROOT,"built:%02d-%02d",group+1,i);
                JSONObject bubble=new JSONObject();
                put(bubble,"id",id);put(bubble,"title",GROUPS[group][i+1]);
                put(bubble,"description","Icône de la famille "+GROUPS[group][0]+".");
                put(bubble,"icon",key);put(bubble,"children",new JSONArray());
                put(bubble,"steps",new JSONArray());put(nodes,id,bubble);children.put(id);
            }
        }
        return true;
    }
    private static void put(JSONObject obj,String name,Object value){try{obj.put(name,value);}catch(Exception ex){throw new IllegalStateException(ex);}}
    /** Import bounded bitmap; preserve source alpha in a compact 256px PNG. Legacy JPEG remains readable. */
    static String importCustom(InputStream input,File media)throws IOException{
        if(input==null)throw new IOException("Image inaccessible");
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buf=new byte[8192];int n,total=0;
        while((n=input.read(buf))!=-1){
            total+=n;if(total>8_000_000)throw new IOException("Image trop volumineuse (8 Mo maximum)");
            out.write(buf,0,n);
        }
        byte[] bytes=out.toByteArray();
        BitmapFactory.Options info=new BitmapFactory.Options();info.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,info);
        if(info.outWidth<1||info.outHeight<1||info.outWidth>10000||info.outHeight>10000)
            throw new IOException("Dimensions d'image invalides");
        BitmapFactory.Options sample=new BitmapFactory.Options();sample.inSampleSize=1;
        while(info.outWidth/sample.inSampleSize>384||info.outHeight/sample.inSampleSize>384)
            sample.inSampleSize*=2;
        Bitmap initial=BitmapFactory.decodeByteArray(bytes,0,bytes.length,sample);
        if(initial==null)throw new IOException("Impossible de lire cette image");
        Bitmap square=Bitmap.createBitmap(256,256,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(square); // Fresh ARGB_8888 bitmap is fully transparent.
        float factor=Math.min(256f/initial.getWidth(),256f/initial.getHeight());
        float w=initial.getWidth()*factor,h=initial.getHeight()*factor;
        android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG|android.graphics.Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(initial,null,new android.graphics.RectF((256-w)/2,(256-h)/2,(256+w)/2,(256+h)/2),paint);
        initial.recycle();
        String name=UUID.randomUUID()+".png";
        File target=new File(media,name),temporary=new File(media,name+".tmp");
        try(FileOutputStream file=new FileOutputStream(temporary)){
            if(!square.compress(Bitmap.CompressFormat.PNG,100,file))throw new IOException("Encodage PNG impossible");
            file.getFD().sync();
        }catch(Exception ex){temporary.delete();throw new IOException("Enregistrement icône impossible",ex);}
        finally{square.recycle();}
        if(!temporary.renameTo(target)){temporary.delete();throw new IOException("Enregistrement icône impossible");}
        return "custom:"+name;
    }
}
