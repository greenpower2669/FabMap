package fr.fabmap.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE=101, TAKE_PHOTO=102, EXPORT=103, IMPORT=104, EXPORT_BUBBLE=105, IMPORT_BUBBLE=106, PICK_ICON=107;
    private String pendingExportBubble, pendingImportParent, pendingIconBubble, pendingPhotoBubble;
    private final ArrayList<String> path=new ArrayList<>();
    private JSONObject data, nodes;
    private LinearLayout screen;
    private TextToSpeech voice;
    private boolean edit=false, expanded=false, valley=false;
    private int step=0;
    private final int ink=Color.rgb(27,55,87), blue=Color.rgb(218,234,255);
    private File mediaDir;
    private IconAssets iconAssets;
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private JSONObject obj(String id){return nodes.optJSONObject(id);}
    private String here(){return path.get(path.size()-1);}
    private JSONObject current(){return obj(here());}
    private static JSONObject p(JSONObject o,String k,Object v){try{o.put(k,v);}catch(Exception ignored){}return o;}
    private static JSONArray arr(String... x){JSONArray a=new JSONArray();for(String s:x)a.put(s);return a;}
    private static JSONObject node(String id,String title,String desc,String... children){
        JSONObject n=new JSONObject();p(n,"id",id);p(n,"title",title);p(n,"description",desc);
        p(n,"children",arr(children));p(n,"steps",new JSONArray());return n;
    }
    @Override public void onCreate(Bundle b){
        super.onCreate(b);getWindow().setStatusBarColor(ink);getWindow().setNavigationBarColor(ink);
        mediaDir=new File(getFilesDir(),"media");mediaDir.mkdirs();iconAssets=new IconAssets(this,mediaDir);load();
        if(b!=null){ArrayList<String> old=b.getStringArrayList("path");if(old!=null)for(String id:old)if(obj(id)!=null)path.add(id);
            pendingExportBubble=b.getString("exportBubble");pendingImportParent=b.getString("importParent");valley=b.getBoolean("valley",false);pendingIconBubble=b.getString("iconBubble");pendingPhotoBubble=b.getString("photoBubble");}
        if(path.isEmpty())path.add("home");
        voice=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)voice.setLanguage(Locale.FRENCH);});
        show();
    }
    @Override protected void onSaveInstanceState(Bundle b){b.putStringArrayList("path",new ArrayList<>(path));
        b.putString("exportBubble",pendingExportBubble);b.putString("importParent",pendingImportParent);b.putBoolean("valley",valley);b.putString("iconBubble",pendingIconBubble);b.putString("photoBubble",pendingPhotoBubble);
        super.onSaveInstanceState(b);}
    private byte[] read(InputStream in,int max)throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n,total=0;
        while((n=in.read(buf))!=-1){total+=n;if(total>max)throw new IOException("Fichier trop volumineux");out.write(buf,0,n);}
        return out.toByteArray();
    }
    private void load(){
        File f=new File(getFilesDir(),"map.json");
        try(FileInputStream in=new FileInputStream(f)){
            data=new JSONObject(new String(read(in,5_000_000),StandardCharsets.UTF_8));nodes=data.getJSONObject("nodes");
            if(data.optInt("schema")!=1||obj("home")==null)throw new IOException("Format de mémoire inconnu");
            if(TvDemo.refresh(nodes))save();
        }catch(Exception ex){
            if(f.exists())f.renameTo(new File(getFilesDir(),"map-invalid-"+System.currentTimeMillis()+".json"));
            data=new JSONObject();nodes=new JSONObject();p(data,"schema",1);p(data,"nodes",nodes);
            p(nodes,"home",node("home","Ma maison","Entrez dans une bulle pour découvrir les suivantes.","piece"));
            p(nodes,"piece",node("piece","Ma grande pièce","Ma pièce de vie.","tele","chatgpt"));
            p(nodes,"tele",node("tele","Ma télévision","Retrouver les gestes et les boutons utiles.","telecommande"));
            p(nodes,"telecommande",node("telecommande","Ma télécommande","On peut ajouter sa photographie."));
            TvDemo.refresh(nodes);
            p(nodes,"chatgpt",node("chatgpt","Ouvrir ChatGPT","Le navigateur ouvre ChatGPT ; aucune connexion n'est copiée."));
            p(obj("chatgpt"),"url","https://chatgpt.com");save();
        }
    }
    private void save(){
        File f=new File(getFilesDir(),"map.json");android.util.AtomicFile atomic=new android.util.AtomicFile(f);
        FileOutputStream out=null;
        try{out=atomic.startWrite();out.write(data.toString().getBytes(StandardCharsets.UTF_8));atomic.finishWrite(out);}
        catch(Exception e){if(out!=null)atomic.failWrite(out);Toast.makeText(this,"Sauvegarde impossible",Toast.LENGTH_LONG).show();}
    }
    private GradientDrawable bg(int color,boolean oval){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(oval?90:18));return d;
    }
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
    private void add(LinearLayout v,View child){v.addView(child);}
    private TextView label(String s,int size,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(ink);
        t.setPadding(dp(14),dp(12),dp(14),dp(12));if(bold)t.setTypeface(null,1);return t;
    }
    /** A light glass medallion rather than an opaque square behind the PNG icon. */
    private View iconBadge(JSONObject node,int size){
        ImageView image=iconAssets.representationView(node,size-12);
        String title=node==null?"Bulle":node.optString("title","Bulle");
        FrameLayout frame=new FrameLayout(this);
        GradientDrawable glass=new GradientDrawable();
        glass.setShape(GradientDrawable.OVAL);
        glass.setColor(0x40FFFFFF);
        glass.setStroke(dp(1),0x99FFFFFF);
        frame.setBackground(glass);
        frame.setElevation(dp(2));
        frame.setLayoutParams(new LinearLayout.LayoutParams(dp(size),dp(size)));
        if(image!=null){
            frame.addView(image,new FrameLayout.LayoutParams(dp(size-12),dp(size-12),Gravity.CENTER));
        }else{
            TextView symbol=label("🫧",36,true);symbol.setGravity(Gravity.CENTER);
            frame.addView(symbol,new FrameLayout.LayoutParams(-1,-1));
        }
        frame.setContentDescription("Icône de "+title);
        return frame;
    }
    /** Restrained two-tone ink with a soft shadow, no opaque label bar. */
    private void styleBubbleTitle(TextView title){
        title.setBackground(null);
        title.setTextColor(0xff163A5B);
        title.getPaint().setShader(new LinearGradient(
            0,0,dp(190),0,0xff173A5A,0xff286487,Shader.TileMode.CLAMP));
        title.setShadowLayer(dp(1),0,dp(1),0x62000000);
        title.setPadding(dp(3),dp(7),dp(3),dp(7));
        title.setMinHeight(dp(48));
    }
    private void button(LinearLayout box,String name,Runnable r){
        button(box,name,r,false);
    }
    private void button(LinearLayout box,String name,Runnable r,boolean confirmation){
        TextView t=label(name,confirmation?26:19,true);
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        t.setMinHeight(dp(confirmation?110:64));
        t.setBackground(bg(confirmation?Color.rgb(24,113,56):blue,false));
        if(confirmation)t.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(dp(3),dp(6),dp(3),dp(6));box.addView(t,lp);
        t.setOnClickListener(v->r.run());
        previewOnLongPress(t,name,confirmation?"Valide cette étape et passe à la suivante. Appuie brièvement pour confirmer.":"Appuie brièvement pour activer ce bouton.");
    }
    private void previewOnLongPress(View target,String title,String description){
        target.setOnLongClickListener(v->{
            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
            showPreview(title,description);
            return true; // Consumed: no click, no navigation, no validation.
        });
    }
    private void showPreview(String title,String description){
        LinearLayout content=column();
        content.setPadding(dp(14),dp(14),dp(14),dp(14));
        TextView heading=label(title,32,true);
        heading.setGravity(Gravity.CENTER);
        content.addView(heading);
        if(description!=null&&!description.trim().isEmpty()){
            TextView detail=label(description,24,false);
            detail.setGravity(Gravity.CENTER);
            content.addView(detail);
        }
        ScrollView scroll=new ScrollView(this);
        scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setView(scroll)
            .setPositiveButton("Fermer",(d,w)->{})
            .setNeutralButton("🔊 Réécouter",null)
            .create();
        dialog.setOnDismissListener(d->{if(voice!=null)voice.stop();});
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->
            say(title+(description==null||description.isEmpty()?"":". "+description)));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextSize(20);
        say(title+(description==null||description.isEmpty()?"":". "+description));
    }
    private void zoom(String id){
        if(obj(id)==null)return;path.add(id);expanded=false;step=0;show();
    }
    private void back(){
        if(path.size()>1){path.remove(path.size()-1);expanded=false;step=0;show();}
        else new AlertDialog.Builder(this).setMessage("Quitter FabMap ?").setPositiveButton("Quitter",(d,w)->finish()).setNegativeButton("Rester",null).show();
    }
    @Override public void onBackPressed(){back();}
    private void say(String s){if(voice!=null)voice.speak(s,TextToSpeech.QUEUE_FLUSH,null,"fabmap");}
    private void photoView(LinearLayout box,JSONObject n){
        String img=n.optString("photo","");
        if(img.isEmpty()||!img.matches("[a-zA-Z0-9_-]+\\.(jpg|png)"))return;
        File f=new File(mediaDir,img);if(!f.isFile())return;
        BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=2;
        Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath(),opt);if(b==null)return;
        ImageView v=new ImageView(this);v.setImageBitmap(b);v.setAdjustViewBounds(true);
        v.setMaxHeight(dp(300));box.addView(v,new LinearLayout.LayoutParams(-1,-2));
    }
    private void show(){
        if(valley){showValley();return;}
        if(obj(here())==null){path.clear();path.add("home");}
        JSONObject n=current();
        screen=column();screen.setBackgroundColor(Color.rgb(249,251,255));
        if(android.os.Build.VERSION.SDK_INT>=30)screen.setOnApplyWindowInsetsListener((v,in)->{
            android.graphics.Insets bars=in.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(dp(8),bars.top,dp(8),bars.bottom);return in;
        });else screen.setPadding(dp(8),dp(22),dp(8),dp(12));
        setContentView(screen);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER_VERTICAL);
        TextView up=label("‹ Retour",18,true);nav.addView(up);up.setOnClickListener(v->back());
        TextView home=label("⌂ Accueil",18,true);nav.addView(home);home.setOnClickListener(v->{path.clear();path.add("home");expanded=false;step=0;show();});
        if("home".equals(here())&&path.size()==1){
            TextView settings=label("⚙ Réglages",18,true);
            nav.addView(settings);
            settings.setOnClickListener(v->generalSettings());
            previewOnLongPress(settings,"Réglages généraux","Sauvegarder ou restaurer la mémoire complète.");
        }
        add(screen,nav);
        if(path.size()==1){
            add(screen,label("Touchez une bulle pour entrer. Maintenez le doigt pour agrandir et écouter.",15,false));
        }
        StringBuilder crumbs=new StringBuilder();for(String id:path){if(crumbs.length()>0)crumbs.append(" › ");crumbs.append(obj(id).optString("title"));}
        TextView breadcrumb=label(crumbs.toString(),13,false);add(screen,breadcrumb);
        ScrollView scroll=new ScrollView(this);screen.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout body=column();scroll.addView(body);
        View iconView=iconBadge(n,94);
        if(iconView!=null)add(body,iconView);
        add(body,label(n.optString("title","Bulle"),27,true));
        add(body,bubbleTools(here()));
        String feelings=n.optString("cognitiveFeelings",""),evocation=n.optString("cognitiveEvocation","");
        if(!feelings.isEmpty()||!evocation.isEmpty())
            add(body,label("Ressenti personnel : "+feelings+"\nÉvocations : "+evocation,17,false));
        button(body,"🫧 Explorer la Vallée des bulles (2D)",()->{valley=true;show();});
        String desc=n.optString("description","");if(!desc.isEmpty()){add(body,label(desc,19,false));button(body,"🔊 Écouter",()->say(desc));}
        JSONArray steps=n.optJSONArray("steps");
        if(steps!=null&&steps.length()>0){
            int i=Math.min(step,steps.length()-1);
            String instruction=steps.optString(i);
            add(body,label("Étape "+(i+1)+" / "+steps.length(),17,true));
            add(body,label(instruction,23,true));
            button(body,"🔊 Lire cette étape",()->say(instruction));
            if(i>0)button(body,"‹ Étape précédente",()->{step--;show();});
            if(i<steps.length()-1)button(body,"✓ C'EST FAIT !",()->{step++;show();},true);
            else button(body,"✓ C'EST FAIT ! — Recommencer",()->{step=0;show();},true);
        }
        JSONArray children=n.optJSONArray("children");int count=children==null?0:children.length();
        if(count>0){add(body,label("Explorer les bulles",21,true));for(int start=0;start<Math.min(count,expanded?count:4);start+=2){
            LinearLayout row=new LinearLayout(this);body.addView(row);
            for(int j=start;j<Math.min(start+2,Math.min(count,expanded?count:4));j++){
                String id=children.optString(j);JSONObject child=obj(id);if(child==null)continue;
                LinearLayout tile=column();tile.setGravity(Gravity.CENTER);
                tile.setPadding(dp(7),dp(9),dp(7),dp(9));
                int baseColor=(j%4==0)?blue:(j%4==1?0xffe0f4e8:(j%4==2?0xfff6e6ff:0xffffefd8));
                tile.setBackground(bg(BubbleStyle.blend(BubbleStyle.userColor(child,baseColor),Color.WHITE,.79f),false));
                tile.setElevation(dp(2));
                View pictogram=iconBadge(child,80);
                if(pictogram!=null)tile.addView(pictogram);
                TextView title=label(child.optString("title","Bulle"),19,true);
                title.setGravity(Gravity.CENTER);
                styleBubbleTitle(title);
                LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(-1,-2);
                titleParams.topMargin=dp(8);tile.addView(title,titleParams);
                tile.addView(bubbleTools(id));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
                lp.setMargins(dp(5),dp(6),dp(5),dp(6));row.addView(tile,lp);
                tile.setMinimumHeight(dp(152));
                tile.setOnClickListener(v->zoom(id));
                previewOnLongPress(tile,child.optString("title"),child.optString("description"));
            }
        }}
        if(count>4)button(body,expanded?"Voir moins":"Voir les autres bulles",()->{expanded=!expanded;show();});
        String url=n.optString("url","");
        if(!url.isEmpty())button(body,"↗ Ouvrir le lien",()->{
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
            catch(Exception ex){Toast.makeText(this,"Aucun navigateur disponible",Toast.LENGTH_LONG).show();}
        });
        button(body,"🔎 Rechercher une bulle",this::search);

        button(body,edit?"Terminer les modifications":"✎ Modifier / créer",()->{edit=!edit;show();});
        if(edit){
            button(body,"↗ Relier une bulle existante",this::linkBubble);
            button(body,"✎ Renommer et décrire",this::editBubble);
            if("home".equals(here())){
                button(body,"🫧 Installer les bulles des icônes",this::installIconCatalog);
            }
            button(body,"＋ Ajouter une étape",()->input("Nouvelle étape","Instruction",text->{
                JSONArray a=current().optJSONArray("steps");if(a==null){a=new JSONArray();p(current(),"steps",a);}
                a.put(text);save();show();
            }));
        }
    }
    private TextView valleyControl(String title,Runnable action){
        TextView t=label(title,18,true);t.setGravity(Gravity.CENTER);
        t.setMinHeight(dp(62));t.setBackground(bg(blue,false));
        t.setOnClickListener(v->action.run());
        previewOnLongPress(t,title,"Appuie brièvement pour utiliser ce bouton.");
        return t;
    }
    private void showValley(){
        if(path.isEmpty()||obj(here())==null){path.clear();path.add("home");}
        screen=column();screen.setBackgroundColor(Color.rgb(247,250,255));
        if(android.os.Build.VERSION.SDK_INT>=30)screen.setOnApplyWindowInsetsListener((v,in)->{
            android.graphics.Insets bars=in.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(dp(8),bars.top,dp(8),bars.bottom);return in;
        });else screen.setPadding(dp(8),dp(22),dp(8),dp(12));
        setContentView(screen);
        button(screen,"‹ Revenir au mode guidé",()->{valley=false;show();});
        add(screen,label("🫧 Vallée : "+current().optString("title","Ma mémoire"),23,true));
        add(screen,label("Bulles réelles uniquement. Glisser, pincer pour zoomer ; maintenir pour écouter.",15,false));
        BubbleValleyView map=new BubbleValleyView(this,nodes,here(),iconAssets,new BubbleValleyView.Listener(){
            @Override public void open(String id){
                valley=false;
                if(!here().equals(id))zoom(id);else show();
            }
            @Override public void preview(String title,String detail){showPreview(title,detail);}
            @Override public void add(String id){newBubble(id);}
            @Override public void customize(String id){customizeBubble(id);}
            @Override public void options(String id){bubbleOptions(id);}
        });
        screen.addView(map,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout controls=new LinearLayout(this);
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);
        left.setMargins(dp(3),dp(4),dp(3),dp(4));
        LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);
        right.setMargins(dp(3),dp(4),dp(3),dp(4));
        controls.addView(valleyControl("− Dézoomer",()->map.zoomBy(.75f)),left);
        controls.addView(valleyControl("＋ Zoomer",()->map.zoomBy(1.33f)),right);
        add(screen,controls);
        button(screen,"◎ Recentrer sur les bulles",map::reset);
    }
    private void chooseIcon(String nodeId){
        ArrayList<IconAssets.Option> options=iconAssets.options();
        ArrayList<String> labels=new ArrayList<>();
        for(IconAssets.Option option:options)labels.add(option.label);
        labels.add("＋ Ajouter une image personnelle");
        labels.add("✕ Retirer l'icône de cette bulle");
        new AlertDialog.Builder(this).setTitle("Icône de la bulle")
            .setItems(labels.toArray(new String[0]),(dialog,index)->{
                if(index<options.size()){
                    p(obj(nodeId),"icon",options.get(index).id);save();show();
                }else if(index==options.size()){
                    pendingIconBubble=nodeId;
                    Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent,PICK_ICON);
                }else{obj(nodeId).remove("icon");save();show();}
            }).setNegativeButton("Annuler",null).show();
    }
    private void installIconCatalog(){
        new AlertDialog.Builder(this).setTitle("Bulles des icônes")
            .setMessage("Ajouter sept familles et leurs 56 bulles conceptuelles à l'accueil ? Les images des planches non encore téléversées apparaîtront plus tard ; les textes restent lisibles.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Ajouter",(d,w)->{
                if(IconAssets.installCatalog(nodes)){save();show();
                    Toast.makeText(this,"Catalogue de bulles ajouté",Toast.LENGTH_LONG).show();}
                else Toast.makeText(this,"Catalogue déjà présent",Toast.LENGTH_LONG).show();
            }).show();
    }

    /** Each bubble has the same three visible actions, independent of edit mode. */
    private LinearLayout bubbleTools(String id){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.addView(bubbleChip("🌈✏", "Crayon : couleur et ressentis",()->customizeBubble(id)),
            new LinearLayout.LayoutParams(0,dp(58),1));
        row.addView(bubbleChip("＋", "Ajouter une bulle fille sous cette bulle",()->newBubble(id)),
            new LinearLayout.LayoutParams(0,dp(58),1));
        row.addView(bubbleChip("⚙", "Options de cette bulle",()->bubbleOptions(id)),
            new LinearLayout.LayoutParams(0,dp(58),1));
        return row;
    }
    private TextView bubbleChip(String sign,String description,Runnable action){
        TextView t=label(sign,27,true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(2),dp(2),dp(2),dp(2));
        GradientDrawable outline=bg(0x8BFFFFFF,false);
        outline.setStroke(dp(1),0x77809BAF);
        t.setBackground(outline);
        t.setContentDescription(description);
        t.setOnClickListener(v->action.run());
        previewOnLongPress(t,description,"Appuie brièvement pour cette action.");
        return t;
    }
    private void generalSettings(){
        String[] entries={"Sauvegarder toute ma mémoire","Restaurer toute ma mémoire"};
        new AlertDialog.Builder(this).setTitle("⚙ Réglages généraux")
            .setItems(entries,(d,index)->{
                if(index==0){
                    Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.putExtra(Intent.EXTRA_TITLE,"FabMap-sauvegarde.fabmap");
                    startActivityForResult(i,EXPORT);
                }else{
                    new AlertDialog.Builder(this).setTitle("Remplacer toute ma mémoire ?")
                        .setMessage("La restauration remplace toutes les bulles actuelles. Exporte d'abord une sauvegarde complète.")
                        .setNegativeButton("Annuler",null)
                        .setPositiveButton("Choisir ma sauvegarde",(d2,w)->{
                            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
                            startActivityForResult(i,IMPORT);
                        }).show();
                }
            }).setNegativeButton("Fermer",null).show();
    }
    private void bubbleOptions(String id){
        JSONObject bubble=obj(id);if(bubble==null)return;
        final String[] options={
            "Renommer et décrire cette bulle",
            "Choisir son image de représentation",
            "Choisir son pictogramme",
            "Ajouter une étape à sa procédure",
            "Relier une bulle déjà existante",
            "Exporter cette bulle et ses filles (avec leurs fichiers liés)",
            "Importer des bulles ici",
            "Ajouter une bulle fille"
        };
        new AlertDialog.Builder(this)
            .setTitle("⚙ "+bubble.optString("title","Bulle"))
            .setItems(options,(d,index)->{
                switch(index){
                    case 0:editBubble(id);break;
                    case 1:imageOptions(id);break;
                    case 2:chooseIcon(id);break;
                    case 3:addStep(id);break;
                    case 4:linkBubble(id);break;
                    case 5:exportCurrentBubble(id);break;
                    case 6:importIntoCurrent(id);break;
                    case 7:newBubble(id);break;
                }
            }).setNegativeButton("Fermer",null).show();
    }
    private void imageOptions(String id){
        String[] options={
            "Choisir une image dans mes fichiers",
            "Prendre une photo",
            "Choisir un pictogramme",
            "Retirer mon image de représentation"
        };
        new AlertDialog.Builder(this).setTitle("Image de représentation")
            .setItems(options,(d,index)->{
                if(index==0){
                    pendingPhotoBubble=id;
                    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");
                    startActivityForResult(i,PICK_IMAGE);
                }else if(index==1){
                    pendingPhotoBubble=id;
                    Intent i=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    try{startActivityForResult(i,TAKE_PHOTO);}
                    catch(Exception ex){pendingPhotoBubble=null;
                        Toast.makeText(this,"Appareil photo indisponible",Toast.LENGTH_LONG).show();}
                }else if(index==2){chooseIcon(id);}
                else{
                    JSONObject node=obj(id);
                    if(node!=null){node.remove("photo");save();show();}
                }
            }).setNegativeButton("Annuler",null).show();
    }
    private void addStep(String id){
        input("Nouvelle étape","Instruction",instruction->{
            JSONObject n=obj(id);if(n==null)return;
            JSONArray steps=n.optJSONArray("steps");
            if(steps==null){steps=new JSONArray();p(n,"steps",steps);}
            steps.put(instruction);save();show();
        });
    }
    /** Only the individual bubble receives this cognitive meaning and color. */
    private void customizeBubble(String id){
        JSONObject node=obj(id);if(node==null)return;
        LinearLayout panel=column();
        panel.setPadding(dp(12),dp(12),dp(12),dp(8));
        add(panel,label("Ce que cette bulle évoque pour moi",21,true));
        add(panel,label("Son sens habituel reste intact. Seul mon ressenti et la couleur de cette bulle sont personnalisés.",16,false));
        EditText feelings=new EditText(this);
        feelings.setTextSize(20);feelings.setSingleLine(false);
        feelings.setHint("Sentiments : joie, tristesse, sécurité…");
        feelings.setText(node.optString("cognitiveFeelings",""));
        add(panel,feelings);
        EditText evocation=new EditText(this);
        evocation.setTextSize(20);evocation.setSingleLine(false);evocation.setMinLines(2);
        evocation.setHint("Évocations, souvenirs, contexte personnel…");
        evocation.setText(node.optString("cognitiveEvocation",""));
        add(panel,evocation);
        add(panel,label("Toucher un hexagone ou saisir #RRGGBB",18,true));
        EditText hex=new EditText(this);
        hex.setTextSize(23);hex.setSingleLine(true);
        hex.setHint("#808080");
        hex.setText(node.optString("cognitiveColor",""));
        add(panel,hex);
        final String[] names={
            "Gris","Bleu","Vert","Jaune","Orange","Rouge",
            "Violet","Rose","Turquoise","Marron","Noir","Blanc"
        };
        final String[] colors={
            "#808080","#356BA1","#39845F","#E2BE51","#D98A42","#B9474C",
            "#8060AA","#C7789C","#398B91","#8B644B","#343A40","#FFFFFF"
        };
        ArrayList<HexColorTile> tiles=new ArrayList<>();
        for(int offset=0;offset<colors.length;offset+=3){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            panel.addView(row);
            for(int k=offset;k<Math.min(offset+3,colors.length);k++){
                final int index=k;
                HexColorTile tile=new HexColorTile(this,names[k],Color.parseColor(colors[k]));
                tiles.add(tile);
                tile.setOnClickListener(v->{
                    hex.setText(colors[index]);
                    for(HexColorTile item:tiles)item.selected(item==tile);
                });
                row.addView(tile,new LinearLayout.LayoutParams(0,dp(83),1));
            }
        }
        ScrollView scroll=new ScrollView(this);scroll.addView(panel);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle("🌈✏ Couleur et sémantique personnelle")
            .setView(scroll)
            .setNegativeButton("Annuler",null)
            .setNeutralButton("Réinitialiser",null)
            .setPositiveButton("Enregistrer",null)
            .create();
        dialog.setOnShowListener(d->{
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                String raw=hex.getText().toString().trim();
                String chosen=raw.isEmpty()?"":BubbleStyle.hex(raw);
                if(!raw.isEmpty()&&chosen==null){
                    hex.setError("Couleur attendue : #RRGGBB");return;
                }
                if(chosen.isEmpty())node.remove("cognitiveColor");
                else p(node,"cognitiveColor",chosen);
                String feeling=feelings.getText().toString().trim();
                String memory=evocation.getText().toString().trim();
                if(feeling.isEmpty())node.remove("cognitiveFeelings");
                else p(node,"cognitiveFeelings",feeling);
                if(memory.isEmpty())node.remove("cognitiveEvocation");
                else p(node,"cognitiveEvocation",memory);
                save();show();dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{
                node.remove("cognitiveColor");node.remove("cognitiveFeelings");
                node.remove("cognitiveEvocation");save();show();dialog.dismiss();
            });
        });
        dialog.show();
    }
    private void exportCurrentBubble(String nodeId){
        pendingExportBubble=nodeId;
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);
        String title=obj(nodeId).optString("title","bulle").replaceAll("[^\\\\p{L}\\\\p{N}_-]+","-");
        i.putExtra(Intent.EXTRA_TITLE,"FabMap-"+title+".fabmap");
        startActivityForResult(i,EXPORT_BUBBLE);
    }
    private void importIntoCurrent(String destination){
        new AlertDialog.Builder(this).setTitle("Ajouter des bulles")
            .setMessage("Le paquet sera ajouté sous « "+obj(destination).optString("title")+" ». Vos bulles existantes seront conservées.")
            .setNegativeButton("Annuler",null)
            .setPositiveButton("Choisir le fichier",(d,w)->{
                pendingImportParent=destination;
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(i,IMPORT_BUBBLE);
            }).show();
    }
    private interface Answer{void use(String s);}
    private void input(String title,String hint,Answer callback){
        EditText e=new EditText(this);e.setHint(hint);e.setTextSize(20);e.setSingleLine(false);
        new AlertDialog.Builder(this).setTitle(title).setView(e).setNegativeButton("Annuler",null)
            .setPositiveButton("OK",(d,w)->{String s=e.getText().toString().trim();if(!s.isEmpty())callback.use(s);}).show();
    }
    private void newBubble(){newBubble(here());}
    private void newBubble(String parentId){input("Nouvelle bulle fille","Exemple : Ma voiture",title->{
        JSONObject parent=obj(parentId);if(parent==null)return;
        String id=UUID.randomUUID().toString();p(nodes,id,node(id,title,""));
        JSONArray children=parent.optJSONArray("children");
        if(children==null){children=new JSONArray();p(parent,"children",children);}
        children.put(id);save();valley=false;
        if(!here().equals(parentId))path.add(parentId);
        zoom(id);
    });}
    private void editBubble(){editBubble(here());}
    private void editBubble(String id){input("Nom de la bulle",obj(id).optString("title"),title->{
        p(obj(id),"title",title);save();input("Explication","Texte lu à voix haute",desc->{
            p(obj(id),"description",desc);save();show();});
    });}
    private void linkBubble(){linkBubble(here());}
    private void linkBubble(String parentId){
        ArrayList<String> ids=new ArrayList<>();ArrayList<String> titles=new ArrayList<>();
        JSONArray existing=obj(parentId).optJSONArray("children");
        java.util.Iterator<String> keys=nodes.keys();while(keys.hasNext()){
            String id=keys.next();if(id.equals(parentId)||contains(existing,id))continue;
            ids.add(id);titles.add(obj(id).optString("title"));
        }
        if(ids.isEmpty()){Toast.makeText(this,"Aucune autre bulle disponible",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("Relier sans dupliquer").setItems(titles.toArray(new String[0]),(d,which)->{
            JSONArray branches=obj(parentId).optJSONArray("children");
            if(branches==null){branches=new JSONArray();p(obj(parentId),"children",branches);}
            branches.put(ids.get(which));save();show();
        }).show();
    }
    private boolean contains(JSONArray a,String id){if(a==null)return false;
        for(int i=0;i<a.length();i++)if(id.equals(a.optString(i)))return true;return false;
    }
    private void search(){input("Rechercher dans ma mémoire","Un mot, une pièce, un appareil...",term->{
        ArrayList<String> ids=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();
        java.util.Iterator<String> it=nodes.keys();while(it.hasNext()){String id=it.next();JSONObject n=obj(id);
            if((n.optString("title")+" "+n.optString("description")).toLowerCase(Locale.FRENCH).contains(term.toLowerCase(Locale.FRENCH))){
                ids.add(id);labels.add(n.optString("title"));
            }
        }
        if(ids.isEmpty()){Toast.makeText(this,"Aucun résultat",Toast.LENGTH_LONG).show();return;}
        new AlertDialog.Builder(this).setTitle("Résultats").setItems(labels.toArray(new String[0]),(d,w)->zoom(ids.get(w))).show();
    });}
    @Override protected void onActivityResult(int req,int result,Intent intent){
        super.onActivityResult(req,result,intent);if(result!=RESULT_OK||intent==null)return;
        try{
            if(req==PICK_ICON){
                String nodeId=pendingIconBubble;pendingIconBubble=null;
                JSONObject target=obj(nodeId);
                if(target==null)throw new IOException("Bulle introuvable");
                String icon;
                try(InputStream in=getContentResolver().openInputStream(intent.getData())){
                    icon=IconAssets.importCustom(in,mediaDir);
                }
                p(target,"icon",icon);save();show();
                Toast.makeText(this,"Icône personnelle sauvegardée",Toast.LENGTH_LONG).show();
            }else if(req==TAKE_PHOTO){
                Bitmap b=(Bitmap)intent.getExtras().get("data");
                if(b==null)throw new IOException("Photo indisponible");
                String name=UUID.randomUUID()+".jpg";
                try(FileOutputStream out=new FileOutputStream(new File(mediaDir,name))){b.compress(Bitmap.CompressFormat.JPEG,90,out);}
                String targetId=pendingPhotoBubble;pendingPhotoBubble=null;
                JSONObject target=obj(targetId);if(target==null)throw new IOException("Bulle introuvable");
                p(target,"photo",name);save();show();
            }else if(req==PICK_IMAGE){
                String targetId=pendingPhotoBubble;pendingPhotoBubble=null;
                JSONObject target=obj(targetId);if(target==null)throw new IOException("Bulle introuvable");
                String icon;
                try(InputStream in=getContentResolver().openInputStream(intent.getData())){
                    icon=IconAssets.importCustom(in,mediaDir);
                }
                p(target,"photo",icon.substring(7));save();show();
            }else if(req==EXPORT){exportTo(intent.getData());Toast.makeText(this,"Sauvegarde créée",Toast.LENGTH_LONG).show();}
            else if(req==IMPORT){importFrom(intent.getData());Toast.makeText(this,"Mémoire restaurée",Toast.LENGTH_LONG).show();show();}
            else if(req==EXPORT_BUBBLE){
                String root=pendingExportBubble;pendingExportBubble=null;
                try(OutputStream out=getContentResolver().openOutputStream(intent.getData())){
                    if(out==null)throw new IOException("Fichier inaccessible");
                    BubblePacks.exportPack(nodes,root,mediaDir,out);
                }
                Toast.makeText(this,"Bulle téléchargée avec ses filles",Toast.LENGTH_LONG).show();
            }else if(req==IMPORT_BUBBLE){
                String parent=pendingImportParent;pendingImportParent=null;
                JSONObject merged;
                try(InputStream in=getContentResolver().openInputStream(intent.getData())){
                    if(in==null)throw new IOException("Fichier inaccessible");
                    merged=BubblePacks.importPack(in,nodes,parent,mediaDir,getCacheDir());
                }
                p(data,"nodes",merged);nodes=merged;save();show();
                Toast.makeText(this,"Bulles ajoutées sans effacer la mémoire",Toast.LENGTH_LONG).show();
            }
        }catch(Exception ex){Toast.makeText(this,"Opération impossible : "+ex.getMessage(),Toast.LENGTH_LONG).show();}
    }
    private void exportTo(Uri uri)throws IOException{
        try(ZipOutputStream zip=new ZipOutputStream(getContentResolver().openOutputStream(uri))){
            zip.putNextEntry(new ZipEntry("map.json"));zip.write(data.toString().getBytes(StandardCharsets.UTF_8));zip.closeEntry();
            // Photographs use JPEG; user icons may be transparent PNG or legacy JPEG.
            File[] images=mediaDir.listFiles();if(images!=null)for(File f:images)if(f.isFile()&&f.getName().matches("[a-zA-Z0-9_-]+\\.(jpg|png)")){
                zip.putNextEntry(new ZipEntry("media/"+f.getName()));
                try(FileInputStream in=new FileInputStream(f)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)zip.write(buf,0,n);}
                zip.closeEntry();
            }
        }
    }
    private void importFrom(Uri uri)throws IOException{
        JSONObject incoming=null;File stage=new File(getCacheDir(),"restore-"+UUID.randomUUID());stage.mkdirs();
        int count=0;long total=0;
        try(InputStream source=getContentResolver().openInputStream(uri);ZipInputStream zip=new ZipInputStream(source)){
            ZipEntry e;while((e=zip.getNextEntry())!=null){
                if(++count>500)throw new IOException("Archive trop grande");
                String name=e.getName();if(e.isDirectory())continue;
                int max=name.equals("map.json")?5_000_000:16_000_000;
                byte[] content=read(zip,max);total+=content.length;
                if(total>100_000_000)throw new IOException("Archive trop volumineuse");
                if(name.equals("map.json"))try{incoming=new JSONObject(new String(content,StandardCharsets.UTF_8));}
                    catch(Exception ex){throw new IOException("Données non reconnues");}
                else if(name.matches("media/[a-zA-Z0-9_-]+\\.(jpg|png)")){
                    try(FileOutputStream out=new FileOutputStream(new File(stage,name.substring(6)))){out.write(content);}
                }else throw new IOException("Fichier inattendu dans l'archive");
                zip.closeEntry();
            }
            if(incoming==null||incoming.optInt("schema")!=1||incoming.optJSONObject("nodes")==null
                    ||incoming.optJSONObject("nodes").optJSONObject("home")==null)throw new IOException("Sauvegarde incompatible");
            JSONObject importedNodes=incoming.getJSONObject("nodes");
            java.util.Iterator<String> keys=importedNodes.keys();
            while(keys.hasNext()){
                JSONObject n=importedNodes.optJSONObject(keys.next());if(n==null)throw new IOException("Bulle invalide");
                JSONArray edges=n.optJSONArray("children");if(edges!=null)for(int i=0;i<edges.length();i++)
                    if(importedNodes.optJSONObject(edges.optString(i))==null)throw new IOException("Lien manquant");
                String custom=n.optString("icon","");
                if(IconAssets.customFile(custom)&&!new File(stage,custom.substring(7)).isFile())
                    throw new IOException("Icône personnelle absente de la sauvegarde");
                String photo=n.optString("photo","");
                if(!photo.isEmpty()&&(!photo.matches("[a-zA-Z0-9_-]+\\.jpg")||!new File(stage,photo).exists()))
                    throw new IOException("Photo absente de l'archive");
            }
            File[] staged=stage.listFiles();if(staged!=null)for(File f:staged){
                File out=new File(mediaDir,f.getName());
                try(FileInputStream in=new FileInputStream(f);FileOutputStream dest=new FileOutputStream(out)){
                    byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)dest.write(buf,0,n);
                }
            }
            data=incoming;nodes=importedNodes;path.clear();path.add("home");step=0;expanded=false;save();
        }catch(Exception e){throw new IOException(e.getMessage(),e);}
        finally{File[] staged=stage.listFiles();if(staged!=null)for(File f:staged)f.delete();stage.delete();}
    }
    @Override protected void onDestroy(){if(voice!=null){voice.stop();voice.shutdown();}super.onDestroy();}
}
