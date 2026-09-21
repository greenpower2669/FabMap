package fr.fabmap.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
    private static final int PICK_IMAGE=101, TAKE_PHOTO=102, EXPORT=103, IMPORT=104, EXPORT_BUBBLE=105, IMPORT_BUBBLE=106;
    private String pendingExportBubble, pendingImportParent;
    private final ArrayList<String> path=new ArrayList<>();
    private JSONObject data, nodes;
    private LinearLayout screen;
    private TextToSpeech voice;
    private boolean edit=false, expanded=false;
    private int step=0;
    private final int ink=Color.rgb(27,55,87), blue=Color.rgb(218,234,255);
    private File mediaDir;
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
        mediaDir=new File(getFilesDir(),"media");mediaDir.mkdirs();load();
        if(b!=null){ArrayList<String> old=b.getStringArrayList("path");if(old!=null)for(String id:old)if(obj(id)!=null)path.add(id);
            pendingExportBubble=b.getString("exportBubble");pendingImportParent=b.getString("importParent");}
        if(path.isEmpty())path.add("home");
        voice=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS)voice.setLanguage(Locale.FRENCH);});
        show();
    }
    @Override protected void onSaveInstanceState(Bundle b){b.putStringArrayList("path",new ArrayList<>(path));
        b.putString("exportBubble",pendingExportBubble);b.putString("importParent",pendingImportParent);
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
        }catch(Exception ex){
            if(f.exists())f.renameTo(new File(getFilesDir(),"map-invalid-"+System.currentTimeMillis()+".json"));
            data=new JSONObject();nodes=new JSONObject();p(data,"schema",1);p(data,"nodes",nodes);
            p(nodes,"home",node("home","Ma maison","Entrez dans une bulle pour découvrir les suivantes.","piece"));
            p(nodes,"piece",node("piece","Ma grande pièce","Ma pièce de vie.","tele","chatgpt"));
            p(nodes,"tele",node("tele","Ma télévision","Retrouver les gestes et les boutons utiles.","telecommande","chaines"));
            p(nodes,"telecommande",node("telecommande","Ma télécommande","On peut ajouter sa photographie.","source"));
            p(nodes,"source",node("source","Bouton SOURCE","Appuyer sur SOURCE pour changer l'entrée vidéo."));
            p(nodes,"chaines",node("chaines","Retrouver les chaînes","Un geste à la fois."));
            p(obj("chaines"),"steps",arr("Prends la télécommande.","Appuie sur SOURCE.","Choisis l'entrée reliée à ta box ou à la télévision.","Vérifie que les chaînes s'affichent."));
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
    private void button(LinearLayout box,String name,Runnable r){
        TextView t=label(name,19,true);t.setBackground(bg(blue,false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(dp(3),dp(5),dp(3),dp(5));
        box.addView(t,lp);t.setOnClickListener(v->r.run());
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
        if(img.isEmpty()||!img.matches("[a-zA-Z0-9_-]+\\.jpg"))return;
        File f=new File(mediaDir,img);if(!f.isFile())return;
        BitmapFactory.Options opt=new BitmapFactory.Options();opt.inSampleSize=2;
        Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath(),opt);if(b==null)return;
        ImageView v=new ImageView(this);v.setImageBitmap(b);v.setAdjustViewBounds(true);
        v.setMaxHeight(dp(300));box.addView(v,new LinearLayout.LayoutParams(-1,-2));
    }
    private void show(){
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
        add(screen,nav);
        StringBuilder crumbs=new StringBuilder();for(String id:path){if(crumbs.length()>0)crumbs.append(" › ");crumbs.append(obj(id).optString("title"));}
        TextView breadcrumb=label(crumbs.toString(),13,false);add(screen,breadcrumb);
        ScrollView scroll=new ScrollView(this);screen.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout body=column();scroll.addView(body);
        add(body,label(n.optString("title","Bulle"),27,true));
        String desc=n.optString("description","");if(!desc.isEmpty()){add(body,label(desc,19,false));button(body,"🔊 Écouter",()->say(desc));}
        photoView(body,n);
        JSONArray steps=n.optJSONArray("steps");
        if(steps!=null&&steps.length()>0){
            int i=Math.min(step,steps.length()-1);
            String instruction=steps.optString(i);
            add(body,label("Étape "+(i+1)+" / "+steps.length(),17,true));
            add(body,label(instruction,23,true));
            button(body,"🔊 Lire cette étape",()->say(instruction));
            if(i>0)button(body,"‹ Étape précédente",()->{step--;show();});
            if(i<steps.length()-1)button(body,"C'est fait →",()->{step++;show();});
            else button(body,"Recommencer",()->{step=0;show();});
        }
        JSONArray children=n.optJSONArray("children");int count=children==null?0:children.length();
        if(count>0){add(body,label("Explorer les bulles",21,true));for(int start=0;start<Math.min(count,expanded?count:4);start+=2){
            LinearLayout row=new LinearLayout(this);body.addView(row);
            for(int j=start;j<Math.min(start+2,Math.min(count,expanded?count:4));j++){
                String id=children.optString(j);JSONObject child=obj(id);if(child==null)continue;
                TextView bubble=label(child.optString("title"),18,true);bubble.setGravity(Gravity.CENTER);
                bubble.setBackground(bg((j%4==0)?blue:(j%4==1?0xffe0f4e8:(j%4==2?0xfff6e6ff:0xffffefd8)),true));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(140),1);lp.setMargins(dp(5),dp(6),dp(5),dp(6));row.addView(bubble,lp);
                bubble.setOnClickListener(v->zoom(id));
            }
        }}
        if(count>4)button(body,expanded?"Voir moins":"Voir les autres bulles",()->{expanded=!expanded;show();});
        String url=n.optString("url","");
        if(!url.isEmpty())button(body,"↗ Ouvrir le lien",()->{
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
            catch(Exception ex){Toast.makeText(this,"Aucun navigateur disponible",Toast.LENGTH_LONG).show();}
        });
        button(body,"🔎 Rechercher une bulle",this::search);
        button(body,"📦 Sauvegarder ma mémoire",()->{
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");
            i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"FabMap-sauvegarde.fabmap");
            startActivityForResult(i,EXPORT);
        });
        button(body,"⬇ Télécharger cette bulle et ses filles",this::exportCurrentBubble);
        button(body,"⬆ Importer des bulles ici",this::importIntoCurrent);
        button(body,"📂 Restaurer une sauvegarde",()->new AlertDialog.Builder(this)
            .setMessage("Restaurer remplacera les bulles actuelles. Avez-vous déjà exporté une sauvegarde ?")
            .setNegativeButton("Annuler",null).setPositiveButton("Choisir le fichier",(d,w)->{
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT);
            }).show());
        button(body,edit?"Terminer les modifications":"✎ Modifier / créer",()->{edit=!edit;show();});
        if(edit){
            button(body,"＋ Ajouter une bulle fille",this::newBubble);
            button(body,"↗ Relier une bulle existante",this::linkBubble);
            button(body,"✎ Renommer et décrire",this::editBubble);
            button(body,"📸 Prendre une photo",()->{Intent i=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                try{startActivityForResult(i,TAKE_PHOTO);}catch(Exception ex){Toast.makeText(this,"Pas d'appareil photo disponible",Toast.LENGTH_LONG).show();}
            });
            button(body,"🖼 Choisir une photo",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");
                i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_IMAGE);
            });
            button(body,"＋ Ajouter une étape",()->input("Nouvelle étape","Instruction",text->{
                JSONArray a=current().optJSONArray("steps");if(a==null){a=new JSONArray();p(current(),"steps",a);}
                a.put(text);save();show();
            }));
        }
    }
    private void exportCurrentBubble(){
        pendingExportBubble=here();
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);
        String title=current().optString("title","bulle").replaceAll("[^\\\\p{L}\\\\p{N}_-]+","-");
        i.putExtra(Intent.EXTRA_TITLE,"FabMap-"+title+".fabmap");
        startActivityForResult(i,EXPORT_BUBBLE);
    }
    private void importIntoCurrent(){
        String destination=here();
        new AlertDialog.Builder(this).setTitle("Ajouter des bulles")
            .setMessage("Le paquet sera ajouté sous « "+current().optString("title")+" ». Vos bulles existantes seront conservées.")
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
    private void newBubble(){input("Nom de la nouvelle bulle","Exemple : La radio",title->{
        String id=UUID.randomUUID().toString();p(nodes,id,node(id,title,""));current().optJSONArray("children").put(id);save();zoom(id);
    });}
    private void editBubble(){input("Nom de la bulle",current().optString("title"),title->{
        p(current(),"title",title);save();input("Explication","Texte lu à voix haute",desc->{p(current(),"description",desc);save();show();});
    });}
    private void linkBubble(){
        ArrayList<String> ids=new ArrayList<>();ArrayList<String> titles=new ArrayList<>();
        JSONArray existing=current().optJSONArray("children");
        java.util.Iterator<String> keys=nodes.keys();while(keys.hasNext()){
            String id=keys.next();if(id.equals(here())||contains(existing,id))continue;
            ids.add(id);titles.add(obj(id).optString("title"));
        }
        if(ids.isEmpty()){Toast.makeText(this,"Aucune autre bulle disponible",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("Relier sans dupliquer").setItems(titles.toArray(new String[0]),(d,which)->{
            current().optJSONArray("children").put(ids.get(which));save();show();
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
            if(req==TAKE_PHOTO){
                Bitmap b=(Bitmap)intent.getExtras().get("data");
                if(b==null)throw new IOException("Photo indisponible");
                String name=UUID.randomUUID()+".jpg";
                try(FileOutputStream out=new FileOutputStream(new File(mediaDir,name))){b.compress(Bitmap.CompressFormat.JPEG,90,out);}
                p(current(),"photo",name);save();show();
            }else if(req==PICK_IMAGE){
                String name=UUID.randomUUID()+".jpg";
                try(InputStream in=getContentResolver().openInputStream(intent.getData());FileOutputStream out=new FileOutputStream(new File(mediaDir,name))){
                    byte[] bytes=read(in,16_000_000);out.write(bytes);
                }p(current(),"photo",name);save();show();
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
            File[] images=mediaDir.listFiles();if(images!=null)for(File f:images)if(f.isFile()&&f.getName().matches("[a-zA-Z0-9_-]+\\.jpg")){
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
                else if(name.matches("media/[a-zA-Z0-9_-]+\\.jpg")){
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
