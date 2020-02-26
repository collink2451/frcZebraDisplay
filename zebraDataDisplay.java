import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.stream.Collectors;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class zebraDataDisplay {
  private static String[] matches = null;
  private static JPanel graphicsPanel = new JPanel();
  public static void main(String[] args) throws InterruptedException {
    System.out.println("Initializing...");
    System.out.println("");
    System.out.println("");
    createFrame();
  }

  public static String[] position(String[] data, int teamNum, String mode) {
    ArrayList<Integer> teamIndex = new ArrayList<Integer>();
    int index = 1;
    int outputTeamNum;
    String pos = null;

    for (int i = 0; i < data.length; i++) // Iterates through all of data[] searching
                                          // for team number
    {
      if (data[i].contains("team_key")) { // Searches for "team_key"
        String num = data[i]; // Sets string to line containing "team_key"
        String outputTeamStr = "";
        for (int j = 0; j < num.length(); j++) // Iterates through all characters of string num
        {
          if (Character.isDigit(num.charAt(j))) // Checks if character is a number
          {
            outputTeamStr = outputTeamStr + num.charAt(j); // Adds number to team number string
          }
        }
        if (isNumeric(outputTeamStr)) {
          outputTeamNum = Integer.parseInt(outputTeamStr);
        } else {
          outputTeamNum = 0;
        }
        if (outputTeamNum == teamNum) {
          teamIndex.add(i);
          index++;
        }
      }
    }
    String[] returnPos = null;
    ArrayList<String> posList = new ArrayList<String>();
    try {
      if (mode.equals("x")) {
        index = 0;
        for (int i = teamIndex.get(index); i < data.length; i++) {
          if (data[i].contains("xs")) {

            for (; i < data.length; i++) {
              if (data[i].contains("]")) {
                posList.add(" ");
                if (teamIndex.size() >= index) {

                  i = teamIndex.get(index);
                  index++;
                  break;
                } else {
                  break;
                }
              }
              pos = data[i];
              String xPos = "";
              for (int j = 0; j < pos.length(); j++) // Iterates through all characters of string num
              {
                if (Character.isDigit(pos.charAt(j)) || pos.charAt(j) == '.') // Checks if character is a number
                {
                  xPos = xPos + pos.charAt(j);
                } else if (pos.contains("null")) {
                  xPos = "null";
                }
              }
              posList.add(xPos);
            }
          }
        }
      } else if (mode.equals("y")) {
        index = 0;
        for (int i = teamIndex.get(index); i < data.length; i++) {
          if (data[i].contains("ys")) {

            for (; i < data.length; i++) {
              if (data[i].contains("]")) {
                posList.add(" ");
                if (teamIndex.size() > index) {

                  i = teamIndex.get(index);
                  index++;
                  break;
                } else {
                  break;
                }
              }
              pos = data[i];
              String yPos = "";
              for (int j = 0; j < pos.length(); j++) // Iterates through all characters of string num
              {
                if (Character.isDigit(pos.charAt(j)) || pos.charAt(j) == '.') // Checks if character is a number
                {
                  yPos = yPos + pos.charAt(j);
                } else if (pos.contains("null")) {
                  yPos = "null";
                }
              }
              posList.add(yPos);
            }
          }
        }
      }
    } catch (IndexOutOfBoundsException ex) {
      System.err.println(ex);
    }
    returnPos = posList.toArray(new String[0]);
    return returnPos;
  }

  public static void write(String filename, String[] data) throws IOException {
    BufferedWriter outputWriter = null;
    outputWriter = new BufferedWriter(new FileWriter(filename));
    for (int i = 0; i < data.length; i++) {
      outputWriter.write(data[i]);
      outputWriter.newLine();
    }
    outputWriter.flush();
    outputWriter.close();
  }

  public static String[] read(File file) throws Exception {
    BufferedReader bufReader = new BufferedReader(new FileReader(file));
    ArrayList<String> list = new ArrayList<>();

    String line = bufReader.readLine();
    while (line != null) {
      list.add(line);
      line = bufReader.readLine();
    }

    bufReader.close();

    String[] data = list.toArray(new String[0]);
    return data;
  }

  public static boolean isNumeric(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    return true;
  }

  public static boolean isNumericDouble(String s) {
    try {
      Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    return true;
  }

  public static String[] getMatches(String[] matches) {
    String matchID = "";
    String eventkey = "";

    ArrayList<String> list2 = new ArrayList<>();

    for (int i = 0; i < matches.length; i++) {
      String data[] = null;
      if (matches[i].contains("]")) {
        break;
      }
      if (!matches[i].contains("[")) {
        matchID = matches[i];
        eventkey = "";
        matchID = matchID.replace(" ", "");
        matchID = matchID.replace(" ", "");
        matchID = matchID.replace("\"", "");
        matchID = matchID.replace(",", "");
        for (int j = 0; j < matchID.length(); j++) {
          if (matchID.charAt(j) == '_') {
            break;
          } else if (matchID.charAt(j) != ' ' || matchID.charAt(j) != '\"') {
            eventkey = eventkey + matchID.charAt(j);
          }
        }

        String filename = "data/" + eventkey + "/" + matchID + ".txt";
        File file = new File(filename);

        if (new File("data/" + eventkey + "/").exists() == false) {
          if (new File("data/" + eventkey + "/").mkdir()) {
            System.out.println("Directory is created");
          } else {
            System.out.println("Directory cannot be created");
          }
        }

        if (file.exists() == false) {
          try {
            // curl command string
            String curl = "curl -X GET \"https://www.thebluealliance.com/api/v3/match/" + matchID
                + "/zebra_motionworks\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(curl);

            String result = new BufferedReader(new InputStreamReader(pr.getInputStream())).lines()
                .collect(Collectors.joining("\n"));

            data = result.toString().split("\\r?\\n"); // Seperates result string into string array
          } catch (IOException ex) {
            System.err.println(ex);
          }
          try {
            write(filename, data);
          } catch (IOException ex) {
            System.err.println(ex);
          }
        }
        try {
          data = read(file);
        } catch (Exception ex) {
          System.err.println(ex);
        }
        ArrayList<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(data));
        list2.addAll(list);
      }
    }
    String[] output = list2.toArray(new String[0]);
    return output;
  }

  public static String[] curlTeamData(int teamNum, String eventkey) {
    String[] data = null;
    String filename = "data/" + eventkey + "/frc" + Integer.toString(teamNum) + ".txt";

    File file = new File(filename);

    if (new File("data/" + eventkey + "/").exists() == false) {
      if (new File("data/" + eventkey + "/").mkdir()) {
        System.out.println("Directory is created");
      } else {
        System.out.println("Directory cannot be created");
      }
    }

    if (file.exists() == false) {
      try {
        String curl = "curl -X GET \"https://www.thebluealliance.com/api/v3/team/frc" + Integer.toString(teamNum)
            + "/event/" + eventkey
            + "/matches/keys\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(curl);

        String result = new BufferedReader(new InputStreamReader(pr.getInputStream())).lines()
            .collect(Collectors.joining("\n"));

        data = result.toString().split("\\r?\\n"); // Seperates result string into string array
      } catch (IOException ex) {
        System.err.println(ex);
      }
      try {
        write(filename, data);
      } catch (IOException ex) {
        System.err.println(ex);
      }
    } else {
      try {
        data = read(file);
      } catch (Exception ex) {
        System.err.println(ex);
      }
    }

    return data;
  }

  public static void saveImage(JPanel GraphicsPanel) {
    BufferedImage output = new BufferedImage(GraphicsPanel.getWidth(), GraphicsPanel.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    Graphics g = output.createGraphics();
    GraphicsPanel.paint(g);

    try {
      File outputfile = new File("saved.png");
      ImageIO.write(output, "png", outputfile);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    try {
      File outputfile = new File("saved.png");
      ImageIO.write(output, "png", outputfile);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static JPanel grabAndDraw(JTextField teamNumTextField, String eventKey, JFrame frame, JLayeredPane layeredPane,
      String drawMode, ArrayList<String> arrayList) {
    String[] xPos, yPos;
    if (teamNumTextField.getText().equals("Team #") == false) {
      int teamNum = Integer.parseInt(teamNumTextField.getText());
      matches = curlTeamData(teamNum, eventKey);
      if (arrayList.isEmpty()) {
        matches = curlTeamData(teamNum, eventKey);
      }
      else {
        matches = arrayList.toArray(new String[0]);
      }

      String[] data = getMatches(matches);
      xPos = position(data, teamNum, "x");
      yPos = position(data, teamNum, "y");
    } else {
      xPos = null;
      yPos = null;
    }
    JPanel graphicsPanel = new JPanel();
    graphicsPanel = new DrawPosition(xPos, yPos, frame, drawMode);
    graphicsPanel.setSize(1200, 800);
    graphicsPanel.setBackground(new Color(0, 0, 0, 0));
    layeredPane.add(graphicsPanel, 1);
    graphicsPanel.removeAll();
    return graphicsPanel;
  }

  public static JFrame createFrame() {
    String eventKey = "2019cc";
    int fieldX = 1164;
    int fieldY = 579;
    String drawMode = "norm";
    // String drawMode = "norm"; // Normal Draw
    // String drawMode = "allBlue"; // Draws all on blue side
    // String drawMode = "allRed"; // Draws all on red side
    // String drawMode = "blueMatches"; // Draws only blue matches
    // String drawMode = "redMatches"; // Draws only blue matches
    JFrame frame = new JFrame("FRC Zebra Display");

    frame.setPreferredSize(new Dimension(1920, 1080));

    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setPreferredSize(new Dimension(1180, 595));

    JPanel panel = new JPanel();
    panel.setFocusable(true);

    GridLayout gridBoxLayout = new GridLayout(0, 5);
    JPanel checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(gridBoxLayout);

    JPanel container = new JPanel();
    container.setLayout(new FlowLayout(FlowLayout.LEADING));

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setAlignmentY(Component.TOP_ALIGNMENT);

    panel.setMaximumSize(new Dimension((int) Math.round(frame.getSize().getWidth() - fieldX), fieldY));
    try {
      frame.setIconImage(ImageIO.read(new File("src/icon.png")));
    } catch (IOException ex) {
      System.err.println(ex);
    }

    JButton draw = new JButton("Draw");
    frame.getRootPane().setDefaultButton(draw);
    JButton save = new JButton("Download");
    JButton grabMatches = new JButton("Grab Matches");

    JTextField teamNumTextField = new JTextField(6);
    teamNumTextField.setText("Team #");
    teamNumTextField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        teamNumTextField.setText("");
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
      }
    });

    ArrayList<String> matchesCheckBox = new ArrayList<String>();
    grabMatches.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Grab Matches")) {
          if (!teamNumTextField.getText().equals("Team #")) {
            try {
              checkBoxPanel.removeAll();
              int teamNum = Integer.parseInt(teamNumTextField.getText());
              String[] data = curlTeamData(teamNum, eventKey);
              for (int i = 0; !data[i].contains("]"); i++) {
                if (!data[i].contains("[")) {
                  String matchID = data[i];
                  matchID = matchID.replace(" ", "");
                  matchID = matchID.replace(" ", "");
                  matchID = matchID.replace("\"", "");
                  matchID = matchID.replace(",", "");
                  final String matchIDf = matchID;
                  JCheckBox checkBox = new JCheckBox((i) + ". " + matchID);
                  checkBox.setName(matchID);
                  checkBoxPanel.add(checkBox);
                  frame.pack();
                  checkBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                      JCheckBox checkBox = (JCheckBox) e.getSource();
                      if (checkBox.isSelected()) {
                        try {
                          matchesCheckBox.add(checkBox.getName());
                          System.out.println("Selected " + matchIDf);
                        } catch (Exception ex) {
                          System.err.println(ex);
                        }
                      } else {
                        try {
                          System.out.println("Deselected " + matchIDf);
                          matchesCheckBox.remove(checkBox.getName());
                        } catch (Exception ex) {
                          System.err.println(ex);
                        }
                      }
                    }
                  });
                }
              }
            } catch (NumberFormatException ex) {
              System.err.println(ex);
            }
          }
        }
      };
    });

    graphicsPanel = grabAndDraw(teamNumTextField, eventKey, frame, layeredPane, "null", null);
    draw.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Draw")) {
          try {
            graphicsPanel = grabAndDraw(teamNumTextField, eventKey, frame, layeredPane, drawMode, matchesCheckBox);
          } catch (Exception ex) {
            System.err.println(ex);
          }
        }
      }
    });

    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Download")) {
          saveImage(graphicsPanel);
        }
      }
    });

    draw.setBounds(0, 25, 100, 40);
    draw.setAlignmentX(Component.LEFT_ALIGNMENT);
    draw.setAlignmentY(Component.BOTTOM_ALIGNMENT);

    panel.add(teamNumTextField);
    panel.add(draw);
    panel.add(save);
    panel.add(grabMatches);

    layeredPane.add(graphicsPanel, 0);
    container.add(layeredPane, BorderLayout.WEST);
    container.add(panel, BorderLayout.EAST);
    container.add(checkBoxPanel, BorderLayout.SOUTH);
    frame.add(container);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

    return frame;
  }
}

class DrawPosition extends JPanel {
  private static final long serialVersionUID = 1L;

  private String[] xPos, yPos;
  private String mode;

  public DrawPosition(String[] xPos, String[] yPos, JFrame frame, String mode) {
    this.xPos = xPos;
    this.yPos = yPos;
    this.mode = mode;
    this.removeAll();
  }

  public static boolean isNumericDouble(String s) {
    try {
      Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    return true;
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.clearRect(0, 0, 1180, 600);
    //Int[] colorsR
    //Int[] colorsG
    //Int[] colorsB
    try {
      java.awt.Image img = ImageIO.read(new File("src/2019_field.png"));
      g.drawImage(img, 0, 0, null);
    } catch (IOException ex) {
      System.err.println(ex);
    }

    if (xPos == null) {
      return;
    }

    g.setColor(new Color(0, 0, 0, 0));
    double scale = 21.4485049833887043;
    int alliance = 0; // 0 is null, 1 is blue, 2 is red
    int j = 0;
    int imageCenterX = 582;
    int imageCenterY = 289;
    g.setColor(new Color(255, 0, 0, 8));
    for (int i = 0; i < xPos.length; i++) {
      if (isNumericDouble(xPos[i]) == false) {
        alliance = 0;

        if (i < xPos.length - 1) {
          j = i + 1;
        }
        if (isNumericDouble(xPos[j]) == false) {
        } else if (((int) Math.round((Double.parseDouble(xPos[j])) * scale) - 5) > 581) {
          alliance = 1;

        } else if (((int) Math.round((Double.parseDouble(xPos[j])) * scale) - 5) < 581) {
          alliance = 2;
        } else {

        }

      }
      if (mode.equals("norm")) {
        try {
          int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale) - 12;
          int y = 570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
          g.fillOval(x, y, 25, 25);
        } catch (NumberFormatException ex) {
          System.err.println(ex);
        }
      } else if (mode.equals("allRed")) {
        if (alliance == 1) {
          try {
            int x = (-(((int) Math.round((Double.parseDouble(xPos[i])) * scale)) - imageCenterX) + imageCenterX);
            int y = (-((570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale)) - imageCenterY) + imageCenterY);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        } else if (alliance == 2) {
          try {
            int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
            int y = 570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        }
      } else if (mode.equals("allBlue")) {
        if (alliance == 1) {
          try {
            int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
            int y = 570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        } else if (alliance == 2) {
          try {
            int x = (-(((int) Math.round((Double.parseDouble(xPos[i])) * scale)) - imageCenterX) + imageCenterX);
            int y = (-((570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale)) - imageCenterY) + imageCenterY);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        }
      } else if (mode.equals("blueMatches")) {
        if (alliance == 1) {
          try {
            int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
            int y = 570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        }
      } else if (mode.equals("redMatches")) {
        if (alliance == 2) {
          try {
            int x = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
            int y = 570 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
            g.fillOval(x, y, 10, 10);
          } catch (NumberFormatException ex) {
            System.err.println(ex);
          }
        }
      } else if (mode.equals("null")) {
        return;
      }
    }
  }
}