import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.stream.Collectors;

public class zebraDataDisplay {
  public static void main(String[] args) {
    System.out.println("Initializing...");
    System.out.println("");
    System.out.println("");
    System.out.println("");
    ImagePanel panel = new ImagePanel(new ImageIcon("assets/field720.png").getImage());

    JFrame frame = new JFrame("THE Jframe");
    frame.getContentPane().add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}

class ImagePanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private Image img;

  public ImagePanel(String img) {
    this(new ImageIcon(img).getImage());
  }

  public ImagePanel(Image img) {
    this.img = img;
    Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setSize(size);
    setLayout(null);
  }

  public void paintComponent(Graphics g) {

    String[] xPos = position(2451, "x");
    //String[] yPos = position(2451, "y");

    g.drawImage(img, 0, 0, null);
    g.fillRect(0, 0, 100, 100);

    int x = 500;
    int y = 500;
    g.fillOval(x, y, 10, 10);
  }

  public static String[] position(int teamNum, String positionType) {
    String[] output = null;
    
    try{
      String str = "curl -X GET \"https://www.thebluealliance.com/api/v3/match/2019cc_qm1/zebra_motionworks\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
      Runtime rt = Runtime.getRuntime();
      Process pr = rt.exec(str);

      String result = new BufferedReader(
                            new InputStreamReader(pr.getInputStream()))
                                .lines()
                                .collect(Collectors.joining("\n"));

      output = result.toString().split("\\r?\\n");
    }
    catch(IOException ex)
    {
      System.err.println(ex);
    }
    int index = 0;
    
    int outputTeamNum;
    for(int i = 0; i < output.length && index < 6; i++)
    {
      String line = output[i];
      if(line.contains("team_key")){
        index++;
        String num = output[i];
        String outputTeamStr = "";
        for(int j = 0; j < num.length(); j++)
        {
          if(Character.isDigit(num.charAt(j)))
          {
            outputTeamStr = outputTeamStr + num.charAt(j);
          }
        }
        System.out.println(outputTeamStr);
        if(isNumeric(outputTeamStr))
        {
            outputTeamNum = Integer.parseInt(outputTeamStr);
        }
        else
        {
          System.out.println("Team number is non-numeric");
        }
      }
    }
    return output;
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
}