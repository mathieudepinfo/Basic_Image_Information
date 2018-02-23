import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.WritableRaster;
import kdtree.*;

/**
 * <p> Class used to compress a given image.</p> 
 * <p>the static value L2N_COLORS is log2 of the maximum number of colors we want in the color list, it should not be greater than 8 </p>
 */
public class Quantification {
	
	public static final int L2N_COLORS=4;
	/**
	 * Public static function that returns the index of a given value in an array
	 * @param int[] palette : array of integer
	 * @param int valeur 
	 * @return the index if it is in, -1 otherwise
	 */
	public static int getIndex(int[] palette, int valeur){
	//fonction qui permet de retrouve l'index d'une couleur dans la palette, -1 si pas dans la palette
		for(int i=0;i<palette.length;i++){
			if(palette[i]==valeur){
				return i;
			}
		}
		return -1;
	}
	/**
	 * Public static function that compress a given image. It indexes each pixel in an array of colors of size 2^L2N_COLORS
	 * @param image
	 * @return a compressed BufferedImage
	 */
	public static BufferedImage compresse(BufferedImage image){
		
		List<Point> listePoints =new ArrayList<Point>();//liste des pixels de l'image
		int R=0;int V=0;int B=0;
		Color pixelcolor; 
		//creation d'une liste de points contenant tout les pixels 
		for(int i=0;i<image.getWidth();i++){
			
			for(int j=0;j<image.getHeight();j++){
				
				pixelcolor=new Color(image.getRGB(i, j));
				R=pixelcolor.getRed();
				V=pixelcolor.getGreen();
				B=pixelcolor.getBlue();
				listePoints.add(new Point(R,V,B,0));
			}
		}
		//on cree un Kdtree de la bonne taille avec tous les pixels de l'image
		KdTree tree=new KdTree(listePoints,3,L2N_COLORS+1);
		
		//on remplit la palette, chaque point a un indice
		List<Point> palette=tree.getColors(L2N_COLORS);
		int nb_couleurs=palette.size();
		for(int i=0;i<nb_couleurs;i++){
			palette.get(i).setOneCoord(3,i);
		}
		//cette fois on fait un Kdtree avec la palette sans prendre en compte la dimension de l index
		KdTree tree_palette=new KdTree(palette,3,L2N_COLORS+1);
		Point p=new Point(0,0,0);
		Point g;
		
		int ig=0;
		byte index[]=new byte[image.getWidth()*image.getHeight()];
		//on cree une nouvelle liste de pixels en remplacant chaque pixels par l'indice de son voisin le plus proche dans la palette
		for(int i=0;i<image.getHeight();i++){
			for(int j=0;j<image.getWidth();j++){
				
				pixelcolor=new Color(image.getRGB(j, i));
				
				R=pixelcolor.getRed();
				V=pixelcolor.getGreen();
				B=pixelcolor.getBlue();
				
				p.setCoord(R,V,B);
				g=tree_palette.getNearestNeighbor(p);//g est le point de la palette correspondant à p
				ig=g.getCoord(3);//on transforme g en une couleur 0xRRVVBB
				index[i*image.getWidth()+j]=(byte)ig;//on met l'indice de g dans la palette
			}
		}
		//Creation de la nouvelle BufferedImage compressee
		byte r[]=new byte[nb_couleurs];
		byte v[]=new byte[nb_couleurs];
		byte b[]=new byte[nb_couleurs];
		
		for(int ind=0;ind<nb_couleurs;ind++){
			for(int j=0;j<nb_couleurs;j++){				
				if(palette.get(j).getCoord(3)==ind){
					
					r[ind]=(byte) palette.get(j).getCoord(0);
					v[ind]=(byte) palette.get(j).getCoord(1);
					b[ind]=(byte) palette.get(j).getCoord(2);
					break;
				}
				
			}
		}
		DataBufferByte dataBuffer = new DataBufferByte(index, image.getWidth()*image.getHeight());
		WritableRaster raster=Raster.createPackedRaster(dataBuffer,image.getWidth(),image.getHeight(),8,null);
		IndexColorModel cm=new IndexColorModel(8,nb_couleurs,r,v,b);
		BufferedImage image_compressee=new BufferedImage(cm,raster,true,null);
		
		System.out.println("Compression reussie");
		
		return image_compressee;
		
		
	}
	
}
	
