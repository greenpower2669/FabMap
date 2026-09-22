import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Build-time only: do not package the 1MB+ source boards in the APK. */
public final class ExtractIconBoards {
    private static final String[][] BOARDS={
        {"01-themes.png","01"},
        {"02-actions.png","02"},
        {"03-etats.png","03"},
        {"04-navigation.png","04"},
        {"05-procedure.png","05"},
        {"06-bulles.png","06"},
        {"07-reperage.png","07"}
    };
    public static void main(String[] args)throws Exception{
        File target=new File("app/src/main/assets/icons/default");
        if(!target.isDirectory()&&!target.mkdirs())throw new Exception("Cannot create icon output");
        int done=0;
        for(String[] board:BOARDS){
            File source=new File("assets/icons/boards/"+board[0]);
            if(!source.isFile())continue; // Fab may upload the remaining boards later.
            BufferedImage image=ImageIO.read(source);
            if(image==null||image.getWidth()<900||image.getHeight()<650)
                throw new Exception("Unexpected icon board: "+source);
            final int[] xs=image.getWidth()==1491?new int[]{193,546,898,1250}:
                new int[]{image.getWidth()*13/100,image.getWidth()*37/100,
                          image.getWidth()*60/100,image.getWidth()*84/100};
            final int[] ys=image.getHeight()==1055?new int[]{372,730}:
                new int[]{image.getHeight()*37/100,image.getHeight()*72/100};
            int crop=image.getWidth()==1491?290:image.getWidth()*19/100;
            for(int i=0;i<8;i++){
                int x=xs[i%4]-crop/2,y=ys[i/4]-crop/2;
                if(x<0||y<0||x+crop>image.getWidth()||y+crop>image.getHeight())
                    throw new Exception("Icon outside board: "+source+" index "+i);
                BufferedImage small=new BufferedImage(144,144,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g=small.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(image,0,0,144,144,x,y,x+crop,y+crop,null);g.dispose();
                File output=new File(target,board[1]+"-"+String.format("%02d",i)+".png");
                if(!ImageIO.write(small,"png",output))throw new Exception("PNG encoder unavailable");
                done++;
            }
        }
        System.out.println("FabMap optimized icon sprites: "+done);
    }
}
