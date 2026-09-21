package fr.fabmap.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Portable, offline bubble/procedure packages. Never replaces the whole memory. */
final class BubblePacks {
    private static final int MAX_META=5_000_000, MAX_PHOTO=16_000_000;
    private static final long MAX_TOTAL=100_000_000L;
    private static final int MAX_ENTRIES=500, MAX_NODES=400;
    private static final String KIND="fabmap-bubble-pack";
    private BubblePacks(){}

    private static void put(JSONObject object,String key,Object value)throws IOException{
        try{object.put(key,value);}catch(JSONException ex){throw new IOException("Données JSON invalides",ex);}
    }
    private static JSONObject parse(byte[] bytes)throws IOException{
        try{return new JSONObject(new String(bytes,StandardCharsets.UTF_8));}
        catch(JSONException ex){throw new IOException("Fichier de bulles non reconnu",ex);}
    }
    private static byte[] read(InputStream in,int max)throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buf=new byte[8192];int n,size=0;
        while((n=in.read(buf))!=-1){
            size+=n;if(size>max)throw new IOException("Fichier trop volumineux");
            out.write(buf,0,n);
        }
        return out.toByteArray();
    }
    private static boolean photoName(String name){
        return name.matches("[a-zA-Z0-9_-]+\\.jpg");
    }
    private static void streamFile(File file,ZipOutputStream zip,long[] size)throws IOException{
        try(InputStream in=new FileInputStream(file)){
            byte[] buf=new byte[8192];int n;long local=0;
            while((n=in.read(buf))!=-1){
                local+=n;size[0]+=n;
                if(local>MAX_PHOTO||size[0]>MAX_TOTAL)throw new IOException("Photos trop volumineuses");
                zip.write(buf,0,n);
            }
        }
    }
    static void exportPack(JSONObject nodes,String rootId,File media,OutputStream destination)throws IOException{
        if(nodes.optJSONObject(rootId)==null)throw new IOException("Bulle absente");
        JSONObject chosen=new JSONObject();
        Set<String> photos=new LinkedHashSet<>(),visited=new LinkedHashSet<>();
        ArrayDeque<String> waiting=new ArrayDeque<>();waiting.add(rootId);
        while(!waiting.isEmpty()){
            String id=waiting.removeFirst();
            if(!visited.add(id))continue;
            if(visited.size()>MAX_NODES)throw new IOException("Trop de bulles dans ce paquet");
            JSONObject node=nodes.optJSONObject(id);
            if(node==null)throw new IOException("Lien vers une bulle absente");
            put(chosen,id,node);
            String photo=node.optString("photo","");
            if(!photo.isEmpty()){
                if(!photoName(photo)||!new File(media,photo).isFile())throw new IOException("Photo manquante : "+photo);
                photos.add(photo);
            }
            JSONArray children=node.optJSONArray("children");
            if(children!=null)for(int i=0;i<children.length();i++)waiting.add(children.optString(i));
        }
        JSONObject packageInfo=new JSONObject();
        put(packageInfo,"kind",KIND);put(packageInfo,"schema",1);
        put(packageInfo,"root",rootId);put(packageInfo,"nodes",chosen);
        byte[] json=packageInfo.toString().getBytes(StandardCharsets.UTF_8);
        if(json.length>MAX_META)throw new IOException("Descriptions trop volumineuses");
        if(photos.size()+1>MAX_ENTRIES)throw new IOException("Trop de photos");
        try(ZipOutputStream zip=new ZipOutputStream(destination)){
            zip.putNextEntry(new ZipEntry("bubble.json"));zip.write(json);zip.closeEntry();
            long[] total={json.length};
            for(String photo:photos){
                zip.putNextEntry(new ZipEntry("media/"+photo));
                streamFile(new File(media,photo),zip,total);
                zip.closeEntry();
            }
        }
    }

    static JSONObject importPack(InputStream source,JSONObject existing,String parentId,
                                 File media,File cache)throws IOException{
        if(existing.optJSONObject(parentId)==null)throw new IOException("Bulle d'accueil introuvable");
        File staged=new File(cache,"fabmap-pack-"+UUID.randomUUID());
        if(!staged.mkdir())throw new IOException("Espace temporaire indisponible");
        JSONObject packageInfo=null;
        Set<String> seen=new HashSet<>();
        long total=0;int entries=0;
        ArrayList<File> installed=new ArrayList<>();
        try{
            try(ZipInputStream zip=new ZipInputStream(source)){
                ZipEntry entry;
                while((entry=zip.getNextEntry())!=null){
                    if(++entries>MAX_ENTRIES)throw new IOException("Trop de fichiers");
                    String name=entry.getName();
                    if(entry.isDirectory()||!seen.add(name))throw new IOException("Entrée invalide ou dupliquée");
                    if(name.equals("bubble.json")){
                        byte[] bytes=read(zip,MAX_META);total+=bytes.length;
                        packageInfo=parse(bytes);
                    }else if(name.matches("media/[a-zA-Z0-9_-]+\\.jpg")){
                        byte[] bytes=read(zip,MAX_PHOTO);total+=bytes.length;
                        try(OutputStream out=new FileOutputStream(new File(staged,name.substring(6)))){
                            out.write(bytes);
                        }
                    }else throw new IOException("Fichier inattendu");
                    if(total>MAX_TOTAL)throw new IOException("Paquet trop volumineux");
                    zip.closeEntry();
                }
            }
            if(packageInfo==null||!KIND.equals(packageInfo.optString("kind"))
                    ||packageInfo.optInt("schema")!=1)throw new IOException("Ce fichier n'est pas un paquet FabMap");
            JSONObject imported=packageInfo.optJSONObject("nodes");
            String root=packageInfo.optString("root","");
            if(imported==null||imported.length()==0||imported.length()>MAX_NODES
                    ||imported.optJSONObject(root)==null)throw new IOException("Bulle de départ absente");
            HashMap<String,String> idMap=new HashMap<>(),photoMap=new HashMap<>();
            Iterator<String> keys=imported.keys();
            while(keys.hasNext()){
                String old=keys.next();
                if(imported.optJSONObject(old)==null)throw new IOException("Bulle invalide");
                idMap.put(old,UUID.randomUUID().toString());
            }
            JSONObject updated=parse(existing.toString().getBytes(StandardCharsets.UTF_8));
            keys=imported.keys();
            while(keys.hasNext()){
                String oldId=keys.next();JSONObject sourceNode=imported.optJSONObject(oldId);
                JSONObject copy=parse(sourceNode.toString().getBytes(StandardCharsets.UTF_8));
                put(copy,"id",idMap.get(oldId));
                JSONArray links=sourceNode.optJSONArray("children");
                JSONArray mapped=new JSONArray();
                if(links!=null)for(int i=0;i<links.length();i++){
                    String mappedId=idMap.get(links.optString(i));
                    if(mappedId==null)throw new IOException("Lien vers une bulle hors du paquet");
                    mapped.put(mappedId);
                }
                put(copy,"children",mapped);
                String oldPhoto=sourceNode.optString("photo","");
                if(!oldPhoto.isEmpty()){
                    if(!photoName(oldPhoto)||!new File(staged,oldPhoto).isFile())
                        throw new IOException("Photo absente : "+oldPhoto);
                    if(!photoMap.containsKey(oldPhoto))photoMap.put(oldPhoto,UUID.randomUUID()+".jpg");
                    put(copy,"photo",photoMap.get(oldPhoto));
                }
                put(updated,idMap.get(oldId),copy);
            }
            JSONObject parent=updated.optJSONObject(parentId);
            JSONArray links=parent.optJSONArray("children");
            if(links==null){links=new JSONArray();put(parent,"children",links);}
            links.put(idMap.get(root));
            // Copy media only after the whole graph has passed validation.
            for(Map.Entry<String,String> entry:photoMap.entrySet()){
                File target=new File(media,entry.getValue());
                try(InputStream in=new FileInputStream(new File(staged,entry.getKey()));
                    OutputStream out=new FileOutputStream(target)){
                    byte[] buf=new byte[8192];int n;
                    while((n=in.read(buf))!=-1)out.write(buf,0,n);
                }
                installed.add(target);
            }
            return updated;
        }catch(Exception error){
            for(File file:installed)file.delete();
            if(error instanceof IOException)throw (IOException)error;
            throw new IOException("Importation impossible",error);
        }finally{
            File[] files=staged.listFiles();if(files!=null)for(File file:files)file.delete();
            staged.delete();
        }
    }
}
