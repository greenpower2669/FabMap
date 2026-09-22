package fr.fabmap.app;

import org.json.JSONArray;
import org.json.JSONObject;

/** Correct the built-in television walkthrough, never overwrite user's edited nodes. */
final class TvDemo {
    private static final String OLD_DESC="Retrouver les gestes et les boutons utiles.";
    private static final String NEW_DESC="Allumer la télévision avec la télécommande, puis choisir le cas affiché à l'écran.";
    private static final String OLD_STEPS_0="Prends la télécommande.";
    private TvDemo(){}
    private static void put(JSONObject o,String key,Object value){
        try{o.put(key,value);}catch(Exception ex){throw new IllegalStateException(ex);}
    }
    private static JSONArray array(String... values){
        JSONArray out=new JSONArray();for(String v:values)out.put(v);return out;
    }
    private static JSONObject bubble(String id,String title,String description,String... links){
        JSONObject n=new JSONObject();put(n,"id",id);put(n,"title",title);
        put(n,"description",description);put(n,"children",array(links));
        put(n,"steps",new JSONArray());return n;
    }
    private static boolean contains(JSONArray links,String id){
        if(links!=null)for(int i=0;i<links.length();i++)
            if(id.equals(links.optString(i)))return true;
        return false;
    }
    private static JSONArray without(JSONArray source,String id){
        JSONArray out=new JSONArray();
        if(source!=null)for(int i=0;i<source.length();i++){
            String v=source.optString(i);if(!id.equals(v))out.put(v);
        }
        return out;
    }
    static boolean refresh(JSONObject nodes){
        JSONObject television=nodes.optJSONObject("tele");
        if(television==null||!"Ma télévision".equals(television.optString("title")))return false;
        String desc=television.optString("description","");
        // A personal rewrite must not be replaced by our factory instructions.
        if(!OLD_DESC.equals(desc)&&!NEW_DESC.equals(desc))return false;
        if(nodes.optJSONObject("tv-start")!=null)return false;
        boolean changed=false;
        JSONObject remote=nodes.optJSONObject("telecommande");
        JSONObject source=nodes.optJSONObject("source");
        JSONObject channels=nodes.optJSONObject("chaines");
        JSONArray sourceSteps=source==null?null:source.optJSONArray("steps");
        boolean oldSource=source!=null
            &&"Bouton SOURCE".equals(source.optString("title"))
            &&"Appuyer sur SOURCE pour changer l'entrée vidéo.".equals(source.optString("description"))
            &&(sourceSteps==null||sourceSteps.length()==0);
        JSONArray oldSteps=channels==null?null:channels.optJSONArray("steps");
        boolean oldChannels=channels!=null
            &&"Retrouver les chaînes".equals(channels.optString("title"))
            &&"Un geste à la fois.".equals(channels.optString("description"))
            &&oldSteps!=null&&oldSteps.length()==4
            &&OLD_STEPS_0.equals(oldSteps.optString(0))
            &&"Appuie sur SOURCE.".equals(oldSteps.optString(1))
            &&"Choisis l'entrée reliée à ta box ou à la télévision.".equals(oldSteps.optString(2))
            &&"Vérifie que les chaînes s'affichent.".equals(oldSteps.optString(3));
        String paidId=oldSource?"source":"tv-paid";
        String blackId=oldChannels?"chaines":"tv-black";
        if(oldSource){
            put(source,"title","Vue des chaînes payantes et autres");
            put(source,"description","Si cette vue apparaît, cherche et sélectionne HDMI3.");
            put(source,"steps",array("Cherche et sélectionne HDMI3.",
                "Attends l'affichage. Il peut parfois être long."));
            changed=true;
        }
        if(oldChannels){
            put(channels,"title","Écran noir");
            put(channels,"description","Si l'écran est noir, utilise la télécommande TV.");
            put(channels,"steps",array(
                "Reprends la télécommande TV, celle dont le bouton ON/OFF porte TV à côté.",
                "Appuie sur ce bouton ON/OFF.",
                "Attends l'affichage. Il peut parfois être long."));
            changed=true;
        }
        if(nodes.optJSONObject(paidId)==null){
            JSONObject paid=bubble(paidId,"Vue des chaînes payantes et autres",
                "Si cette vue apparaît, cherche et sélectionne HDMI3.");
            put(paid,"steps",array("Cherche et sélectionne HDMI3.",
                "Attends l'affichage. Il peut parfois être long."));
            put(paid,"icon","built:05-05");
            put(nodes,paidId,paid);changed=true;
        }
        if(nodes.optJSONObject(blackId)==null){
            JSONObject black=bubble(blackId,"Écran noir",
                "Si l'écran est noir, utilise la télécommande TV.");
            put(black,"steps",array(
                "Reprends la télécommande TV, celle dont le bouton ON/OFF porte TV à côté.",
                "Appuie sur ce bouton ON/OFF.",
                "Attends l'affichage. Il peut parfois être long."));
            put(nodes,blackId,black);changed=true;
        }
        String guideId="tv-start";
        JSONObject existingLegacy=nodes.optJSONObject("fabmap-tv-real");
        if(existingLegacy!=null)guideId="fabmap-tv-real"; // Existing user may have opened this path.
        if(nodes.optJSONObject(guideId)==null){
            JSONObject guide=bubble(guideId,"Allumer la télévision",
                "D'abord allumer la TV. Ensuite, choisir seulement le cas observé à l'écran.",
                paidId,blackId);
            put(guide,"icon","built:01-02");
            put(guide,"steps",array(
                "Allume la télévision avec la télécommande, au moyen du bouton ON/OFF indiqué dessus.",
                "Observe l'écran puis choisis ci-dessous le cas qui correspond à ce que tu vois."));
            put(nodes,guideId,guide);changed=true;
        }
        if(remote!=null&&"Ma télécommande".equals(remote.optString("title"))
                &&"On peut ajouter sa photographie.".equals(remote.optString("description"))){
            put(remote,"description","La télécommande TV porte le bouton ON/OFF marqué TV à côté. Tu peux ajouter sa photographie.");
            if(oldSource&&contains(remote.optJSONArray("children"),"source"))
                put(remote,"children",without(remote.optJSONArray("children"),"source"));
            changed=true;
        }
        JSONArray roots=television.optJSONArray("children");
        if(roots==null)roots=new JSONArray();
        if(oldChannels&&contains(roots,"chaines")){roots=without(roots,"chaines");changed=true;}
        if(!contains(roots,guideId)){
            JSONArray ordered=new JSONArray();ordered.put(guideId);
            for(int i=0;i<roots.length();i++)ordered.put(roots.optString(i));
            roots=ordered;changed=true;
        }
        if(changed||OLD_DESC.equals(desc)){
            put(television,"children",roots);
            put(television,"description",NEW_DESC);
            return true;
        }
        return false;
    }
}
