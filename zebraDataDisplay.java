import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.stream.Collectors;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class zebraDataDisplay {
  public static void main(String[] args) {
    System.out.println("Initializing...");
    System.out.println("");
    System.out.println("");
    ImagePanel panel = new ImagePanel(new ImageIcon("src//*2019_fiel*/.png").getImage()); // Sets background image

    JFrame frame = new JFrame("FRC Zebra Display"); // Sets frame title

    // Sets JFrame icon
    try {
      frame.setIconImage(ImageIO.read(new File("src/icon.png")));
    } catch (IOException ex) {
      System.err.println(ex);
    }
    frameObject(frame);
    frame.getContentPane().add(panel);
    frame.setSize(300,300);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true); // Sets frame visibility

    String[] data = curl();
    String[] xPos = position(data, 2930, "x");
    String[] yPos = position(data, 2930, "y");
    drawPosition(xPos, yPos, frame);
  }
  public static String[] curl() {
    String[] data = null;
    String filename = "test.txt";
    File file = new File(filename);
    
    if(file.exists() == false)
    {
       System.out.println("Doesn't Exist");
       try {
         // curl command string
         String curl = "curl -X GET \"https://www.thebluealliance.com/api/v3/match/2019cc_qm1/zebra_motionworks\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
         Runtime rt = Runtime.getRuntime();
         Process pr = rt.exec(curl);
   
         String result = new BufferedReader(new InputStreamReader(pr.getInputStream())).lines()
             .collect(Collectors.joining("\n"));
   
         data = result.toString().split("\\r?\\n"); // Seperates result string into string array
       } catch (IOException ex) {
         System.err.println(ex);
       }
       try
       {
         System.out.println(data);
         write("test.txt", data);
       }
       catch(IOException ex)
       {
         System.err.println(ex);
       }
    }
    else
    {
       System.out.println("Exists");
    }
    return data;
  }
  public static String[] position(String[] data, int teamNum, String mode) {

    int index = 0;
    int teamIndex = 0;
    int outputTeamNum;
    String pos = null;
    for (int i = 0; i < data.length && index < 6; i++) // Iterates through all of data[] searching
                                                                           // for team number
    {
      String line = data[i];
      if (line.contains("team_key")) { // Searches for "team_key"
        index++; //
        String num = data[i]; // Sets string to line containing "team_key"
        String outputTeamStr = "";
        for (int j = 0; j < num.length(); j++) // Iterates through all characters of string num
        {
          if (Character.isDigit(num.charAt(j))) // Checks if character is a number
          {
            outputTeamStr = outputTeamStr + num.charAt(j); // Adds number to team number string
          }
        }
        if(isNumeric(outputTeamStr))
        {
          outputTeamNum = Integer.parseInt(outputTeamStr);
        }
        else
        {
          outputTeamNum = 0;
        }
        if (outputTeamNum == teamNum) {
          teamIndex = i;
          break;
        }
      }
    }
    String[] returnPos = null;
    ArrayList<String> posList = new ArrayList<String>();
    if (mode.equals("x")) {
      for (int i = teamIndex; i < data.length; i++) {
        if (data[i].contains("xs")) {
          int xs = i;
          for (i = xs; i < data.length; i++) {
            if (data[i].contains("]")) {
              break;
            }
            pos = data[i];
            String xPos = "";
            for (int j = 0; j < pos.length(); j++) // Iterates through all characters of string num
            {
              if (Character.isDigit(pos.charAt(j)) || pos.charAt(j) == '.') // Checks if character is a number
              {
                xPos = xPos + pos.charAt(j);;
              }
              else if (pos.contains("null"))
              {
                xPos = "null";
              }
            }
            posList.add(xPos);
            returnPos = posList.toArray(new String[0]);
            
          }
          break;
        } 
      }
    } 
    else if (mode.equals("y")) 
    {
      for (int i = teamIndex; i < data.length; i++) {
        if (data[i].contains("ys")) {
          int ys = i;
          for (i = ys; i < data.length; i++) {
            if (data[i].contains("]")) {
              break;
            }
            pos = data[i];
            String yPos = "";
            for (int j = 0; j < pos.length(); j++) // Iterates through all characters of string num
            {
              if (Character.isDigit(pos.charAt(j)) || pos.charAt(j) == '.') // Checks if character is a number
              {
                yPos = yPos + pos.charAt(j);;
              }
              else if (pos.contains("null"))
              {
                yPos = "null";
              }
            }
            posList.add(yPos);
            returnPos = posList.toArray(new String[0]);
          }
          break;
        } 
      }
    }
    return returnPos;
  }

  public static void drawPosition(String[] xPos, String[] yPos, JFrame frame) {
    double scale = 21.4485049833887043;
    Graphics g = frame.getGraphics();
    g.setColor(new Color(255, 0, 0, 100));
    for (int i = 0; i < xPos.length; i++) {
      try{
        int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale) - 5;
        int y = 594 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
        g.fillOval(x, y, 25, 25);
      }
      catch(NumberFormatException nfe)  
      {
        System.err.println(nfe);
      }
    }
  }
  public static void write (String filename, String[] data) throws IOException
  {
     BufferedWriter outputWriter = null;
     outputWriter = new BufferedWriter(new FileWriter(filename));
     for (int i = 0; i < data.length; i++) {
       outputWriter.write(data[i]);
       outputWriter.newLine();
     }
     outputWriter.flush();  
     outputWriter.close();  
  }
  public static boolean isNumeric(final String str) {

    // null or empty
    if (str == null || str.length() == 0) {
      return false;
    }

    for (char c : str.toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }

    return true;
  }
  public static void frameObject(JFrame f)
  {   
					//submit button
		JButton b=new JButton("Submit");    
		b.setBounds(100,100,140, 40);    
					//enter name label
		JLabel label = new JLabel();		
		label.setText("Enter Team Number");
		label.setBounds(10, 10, 100, 100);
					//empty label which will show event after button clicked
		JLabel label1 = new JLabel();
		label1.setBounds(10, 110, 200, 100);
					//textfield to enter name
		JTextField textfield= new JTextField();
		textfield.setBounds(110, 50, 130, 30);
					//add to frame
		f.add(label1);
		f.add(textfield);
		f.add(label);
		f.add(b);    
		   
		
							//action listener
		b.addActionListener(new ActionListener() {
	        
			@Override
			public void actionPerformed(ActionEvent arg0) {
					label1.setText("Name has been submitted.");				
			}          
	      });
		}
}

class ImagePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private Image img;

  public ImagePanel(String img) {
    this(new ImageIcon(img).getImage());
  }

  public ImagePanel(Image img) { // Sets image dimensions to match image size
    this.img = img;
    Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setSize(size);
    setLayout(null);
  }

  public void paintComponent(Graphics g) {
    g.drawImage(img, 0, 0, null); // Draws background image
  }
}