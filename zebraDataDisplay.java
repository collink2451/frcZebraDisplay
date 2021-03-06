import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.stream.Collectors;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

public class zebraDataDisplay {
  static String version = "V1.0.2";
  static String[] matches = null;
  static JPanel graphicsPanel = new JPanel();
  static JPanel checkBoxPanel;
  static Properties configFile = new java.util.Properties();
  static ArrayList<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
  static ArrayList<JCheckBox> checkBoxesSelected = new ArrayList<JCheckBox>();
  static String eventKey = null;
  static JCheckBox modeNorm = new JCheckBox("Normal");
  static JCheckBox modeHM = new JCheckBox("Heat Map");
  static JCheckBox modeMC = new JCheckBox("Multi Color");
  static JCheckBox modeAuto = new JCheckBox("Autonomous");
  static JCheckBox modeAlli = new JCheckBox("Alliance");
  static JCheckBox drawNorm = new JCheckBox("Normal");
  static JCheckBox drawAllBlue = new JCheckBox("All on Blue Alliance");
  static JCheckBox drawAllRed = new JCheckBox("All on Red Alliance");
  static JCheckBox drawBlueOnly = new JCheckBox("Blue Data Only");
  static JCheckBox drawRedOnly = new JCheckBox("Red Data Only");
  static String drawMode, colorMode;
  static String prevDrawMode = null;
  static String prevColorMode = null;
  static boolean auto = false;
  static boolean alli = false;
  static int teamNumTemp = 0;

  public static void main(String[] args) throws InterruptedException {
    System.out.println("Initializing...");
    System.out.println("");
    System.out.println("");
    try {
      FileInputStream ip = new FileInputStream(new File("config.cfg"));
      configFile.load(ip);
      eventKey = configFile.getProperty("event");
    } catch (FileNotFoundException ex) {
      System.err.println(ex);
    } catch (IOException ex) {
      System.err.println(ex);
    }

    createFrame();
  }

  public static void bulkDownload(String eventKey) {
    String[] teams;
    ArrayList<String> allMatches = new ArrayList<String>();

    try {
      String curl = "curl -X GET \"https://www.thebluealliance.com/api/v3/event/" + eventKey
          + "/teams/keys\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
      Runtime rt = Runtime.getRuntime();
      Process pr = rt.exec(curl);

      String result = new BufferedReader(new InputStreamReader(pr.getInputStream())).lines()
          .collect(Collectors.joining("\n"));

      teams = result.toString().split("\\r?\\n"); // Seperates result string into string array
      for (int i = 1; i < teams.length; i++) {
        teams[i] = teams[i].replace(" ", "");
        teams[i] = teams[i].replace("\"", "");
        teams[i] = teams[i].replace(",", "");
        teams[i] = teams[i].replace("frc", "");

      }
    } catch (IOException ex) {
      System.err.println(ex);
      teams = null;
    }
    for (int i = 1; i < teams.length; i++) {
      if (teams[i].contains("]")) {
        break;
      }
      String[] matches = curlTeamData(Integer.parseInt(teams[i]), eventKey);
      allMatches.addAll(Arrays.asList(matches));
    }
    String[] matches = allMatches.toArray(new String[0]);
    for (int i = 0; i < matches.length; i++) {
      matches[i] = matches[i].replace("]", "");
    }
    getMatches(matches);

  }

  public static ArrayList<String> position(String[] data, int teamNum, String mode) {
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
    return posList;
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
            if (data[0].contains("{")) {
              write(filename, data);
              try {
                data = read(file);
              } catch (Exception ex) {
                System.err.println(ex);
              }
            }
          } catch (IOException ex) {
            System.err.println(ex);
          }
        } else if (file.exists()) {
          try {
            data = read(file);
          } catch (Exception ex) {
            System.err.println(ex);
            data = null;
          }
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
        String curl = "curl -X GET \"https://www.thebluealliance.com/api/v3/team/frc" + Integer.toString(teamNum)
            + "/event/" + eventkey
            + "/matches/keys\" -H \"accept: application/json\" -H \"X-TBA-Auth-Key: cc7emTYzQsRjewnrxcNwWo913bgbPBPR2UgNmgxWWVoFmZFeRKUTKVNkfNgKD7SN\"";
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(curl);

        int exitCode = pr.waitFor();

        String result = new BufferedReader(new InputStreamReader(pr.getInputStream())).lines()
            .collect(Collectors.joining("\n"));

        data = result.toString().split("\\r?\\n");

        if (exitCode == 6) {
          try {
            data = read(file);
          } catch (Exception ex) {
            System.err.println(ex);
          }
        } else {
          try {
            write(filename, data);
          } catch (IOException ex) {
            System.err.println(ex);
          }
          try {
            data = read(file);
          } catch (Exception ex) {
            System.err.println(ex);
          }
        }
      } catch (IOException ex) {
        System.err.println(ex);
      } catch (InterruptedException e) {
        System.err.println(e);
      }
    }

    return data;
  }

  public static void saveImage(JPanel GraphicsPanel, String teamNum, String teamNum2, String teamNum3) {
    BufferedImage output = new BufferedImage(GraphicsPanel.getWidth(), GraphicsPanel.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    Graphics g = output.createGraphics();
    GraphicsPanel.paint(g);

    if (new File("images/").exists() == false) {
      if (new File("images/").mkdir()) {
        System.out.println("Directory is created");
      } else {
        System.out.println("Directory cannot be created");
      }
    }

    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial Black", Font.PLAIN, 20));

    int lineIndex = 0;
    String draw = null;
    if (prevDrawMode.equals("norm")) {
      draw = "Normal Mode";
    } else if (prevDrawMode.equals("allBlue")) {
      draw = "All Blue";
    } else if (prevDrawMode.equals("allRed")) {
      draw = "All Red";
    } else if (prevDrawMode.equals("blueMatches")) {
      draw = "Blue Only";
    } else if (prevDrawMode.equals("redMatches")) {
      draw = "Red Only";
    }
    String color = null;
    if (prevColorMode.equals("norm")) {
      color = "Normal Color";
    } else if (prevColorMode.equals("heatMap")) {
      color = "Heat Map";
    } else if (prevColorMode.equals("multiColor")) {
      color = "Multicolor";
    }

    if (!alli) {
      String text = teamNum + " - " + eventKey + " - " + draw + " - " + color + "\n";

      String imageName = "images/" + teamNum + "-" + eventKey + "-" + prevDrawMode + "-" + prevColorMode;
      String matchID;
      for (int i = 1; i < matches.length; i++) {
        if (matches[i].contains("]")) {
          break;
        }
        matchID = (matches[i]).replace(eventKey, "");
        matchID = matchID.replace(" ", "");
        matchID = matchID.replace("\"", "");
        matchID = matchID.replace(",", "");
        imageName = imageName + matchID;
        text = text + matchID.replace("_", "") + " ";
        lineIndex++;
        if (lineIndex % 6 == 0) {
          text = text + "\n";
        }
      }
      int x = 20;
      int y = 600;
      for (String line : text.split("\n")) {
        g.drawString(line, x, y += g.getFontMetrics().getHeight());
      }
      imageName = imageName + ".png";
      try {
        File outputfile = new File(imageName);
        ImageIO.write(output, "png", outputfile);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    } else {
      String text = teamNum;
      String imageName = "images/" + teamNum;
      if (isNumeric(teamNum2)) {
        text = text + " - " + teamNum2;
        imageName = imageName + "-" + teamNum2;
      }
      if (isNumeric(teamNum3)) {
        text = text + " - " + teamNum3;
        imageName = imageName + "-" + teamNum3;
      }
      text = text + " - " + eventKey + " - " + draw + " - " + color + "\n";
      imageName = imageName + "-" + eventKey + "-" + prevDrawMode + "-" + prevColorMode;

      int x = 20;
      int y = 600;
      for (String line : text.split("\n")) {
        g.drawString(line, x, y += g.getFontMetrics().getHeight());
      }
      imageName = imageName + ".png";
      try {
        File outputfile = new File(imageName);
        ImageIO.write(output, "png", outputfile);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  public static JPanel grabAndDraw(JTextField teamNumTextField1, JTextField teamNumTextField2,
      JTextField teamNumTextField3, String eventKey, JFrame frame, JLayeredPane layeredPane, String drawMode,
      ArrayList<JCheckBox> arrayList, String colorMode) {

    ArrayList<String> xPosList, yPosList;
    String[] xPos, yPos;
    int teamNum = 0;
    ArrayList<String> matchesArrayList = new ArrayList<String>();

    if (!alli) {
      try {
        for (int i = 0; i < arrayList.size(); i++) {
          matchesArrayList.add(arrayList.get(i).getName());
        }
      } catch (NullPointerException npe) {
        System.err.println(npe);
      }
    }

    if (isNumeric(teamNumTextField1.getText())) {
      teamNum = Integer.parseInt(teamNumTextField1.getText());
      matches = curlTeamData(teamNum, eventKey);
      if (matchesArrayList.isEmpty()) {
        matches = curlTeamData(teamNum, eventKey);
      } else {
        matches = matchesArrayList.toArray(new String[0]);
      }
      String[] data = getMatches(matches);
      xPosList = position(data, teamNum, "x");
      yPosList = position(data, teamNum, "y");
    } else {
      xPosList = null;
      yPosList = null;
    }
    if (alli) {
      if (isNumeric(teamNumTextField2.getText())) {
        teamNum = Integer.parseInt(teamNumTextField2.getText());
        matches = curlTeamData(teamNum, eventKey);
        if (matchesArrayList.isEmpty()) {
          matches = curlTeamData(teamNum, eventKey);
        } else {
          matches = matchesArrayList.toArray(new String[0]);
        }
        String[] data = getMatches(matches);
        xPosList.addAll(position(data, teamNum, "x"));
        yPosList.addAll(position(data, teamNum, "y"));
      }
      if (isNumeric(teamNumTextField3.getText())) {
        teamNum = Integer.parseInt(teamNumTextField3.getText());
        matches = curlTeamData(teamNum, eventKey);
        if (matchesArrayList.isEmpty()) {
          matches = curlTeamData(teamNum, eventKey);
        } else {
          matches = matchesArrayList.toArray(new String[0]);
        }

        String[] data = getMatches(matches);
        xPosList.addAll(position(data, teamNum, "x"));
        yPosList.addAll(position(data, teamNum, "y"));
      }
    }
    try {
      xPos = xPosList.toArray(new String[0]);
      yPos = yPosList.toArray(new String[0]);
    } catch (NullPointerException ex) {
      xPos = null;
      yPos = null;
      System.err.println(ex);
    }

    JPanel graphicsPanel = new JPanel();
    graphicsPanel = new DrawPosition(xPos, yPos, frame, drawMode, colorMode, matches, checkBoxPanel, auto, alli);
    prevDrawMode = drawMode;
    prevColorMode = colorMode;
    graphicsPanel.setSize(1200, 800);
    graphicsPanel.setBackground(new Color(0, 0, 0, 0));
    layeredPane.add(graphicsPanel, 1);
    graphicsPanel.removeAll();

    return graphicsPanel;
  }

  public static JFrame createFrame() {
    int fieldX = 1164;
    drawMode = "norm";
    // norm, allBlue, allRed, blueMatches, redMatches

    colorMode = "norm";
    // norm, heatMap, multiColor

    JFrame frame = new JFrame("FRC Zebra Display");

    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setPreferredSize(new Dimension(1200, 600));

    JPanel panel = new JPanel();
    panel.setFocusable(true);

    GridLayout gridBoxLayout6 = new GridLayout(1, 3);
    JPanel teamInputPanel = new JPanel();
    teamInputPanel.setLayout(gridBoxLayout6);

    GridLayout gridBoxLayout = new GridLayout(0, 5);
    checkBoxPanel = new JPanel();
    checkBoxPanel.setLayout(gridBoxLayout);

    GridLayout gridBoxLayout2 = new GridLayout(2, 1);
    JPanel checkBoxOptions = new JPanel();
    checkBoxOptions.setLayout(gridBoxLayout2);

    GridLayout gridBoxLayout3 = new GridLayout(1, 2);
    JPanel checkBoxes = new JPanel();
    checkBoxes.setLayout(gridBoxLayout3);

    GridLayout gridBoxLayout4 = new GridLayout(6, 1);
    JPanel modeSelection = new JPanel();
    modeSelection.setLayout(gridBoxLayout4);

    GridLayout gridBoxLayout5 = new GridLayout(6, 1);
    JPanel modeOptions = new JPanel();
    modeOptions.setLayout(gridBoxLayout5);

    JPanel sideBar = new JPanel();
    sideBar.setLayout(new GridLayout(3, 1));
    sideBar.setPreferredSize(new Dimension(300, 600));

    JPanel container = new JPanel();
    container.setLayout(new FlowLayout(FlowLayout.LEADING));

    panel.setLayout(new GridLayout(8, 1));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weighty = 2;

    JLabel api = new JLabel("Powered By The Blue Alliance");
    JLabel teamNumLabel = new JLabel("Team Numbers");
    JLabel eventKeyLabel = new JLabel("Event Key");
    JLabel info1 = new JLabel("FRC Zebra Data Display " + version);
    JLabel info2 = new JLabel("Developed for PWNAGE FRC2451");
    JLabel info3 = new JLabel("\u00a9 Collin Koldoff 2020");

    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setAlignmentY(Component.TOP_ALIGNMENT);

    panel.setSize(new Dimension((int) Math.round(frame.getSize().getWidth() - fieldX), 700));
    try {
      frame.setIconImage(ImageIO.read(new File("src/icon.png")));
    } catch (IOException ex) {
      System.err.println(ex);
    }

    frame.setPreferredSize(new Dimension(1920, 1080));

    JButton draw = new JButton("Draw Image");
    frame.getRootPane().setDefaultButton(draw);
    JButton save = new JButton("Download Image");
    JButton grabMatches = new JButton("Grab Matches");

    JButton bulkDownload = new JButton("Bulk Download (Large Internet Usage)");
    JButton bulkDownloadConfirm = new JButton("Are you sure?");

    JTextField teamNumTextField = new JTextField(6);
    teamNumTextField.setText("Team #");
    teamNumTextField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        if (teamNumTextField.getText().equals("Team #")) {
          teamNumTextField.setText("");
        }
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
        if (!isNumeric(teamNumTextField.getText())) {
          teamNumTextField.setText("Team #");
        } else {
          int teamNumTemp2 = Integer.parseInt(teamNumTextField.getText());
          if (teamNumTemp2 == teamNumTemp) {
            checkBoxesSelected.clear();
          }
          teamNumTemp = Integer.parseInt(teamNumTextField.getText());
        }
      }
    });
    JTextField teamNumTextField2 = new JTextField(6);
    teamNumTextField2.setText("Team #2");
    teamNumTextField2.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        teamNumTextField2.setText("");
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
      }
    });
    JTextField teamNumTextField3 = new JTextField(6);
    teamNumTextField3.setText("Team #3");
    teamNumTextField3.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        teamNumTextField3.setText("");
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
      }
    });

    JTextField competitionIDField = new JTextField(6);
    competitionIDField.setText(eventKey);
    competitionIDField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
        try {
          FileInputStream ip = new FileInputStream(new File("config.cfg"));
          configFile.load(ip);
          configFile.setProperty("event", competitionIDField.getText());
          configFile.store(new FileOutputStream("config.cfg"), null);
        } catch (IOException ex) {
          System.err.println(ex);
        }
      }
    });

    JLabel optionHead = new JLabel("Drawing Options");
    optionHead.setSize(100, 10);
    drawNorm.setSize(100, 10);
    drawAllBlue.setSize(100, 10);
    drawAllRed.setSize(100, 10);
    drawBlueOnly.setSize(100, 10);
    drawRedOnly.setSize(100, 10);

    modeOptions.add(optionHead, BorderLayout.NORTH);
    modeOptions.add(drawNorm, BorderLayout.WEST);
    modeOptions.add(drawAllBlue, BorderLayout.WEST);
    modeOptions.add(drawAllRed, BorderLayout.WEST);
    modeOptions.add(drawBlueOnly, BorderLayout.WEST);
    modeOptions.add(drawRedOnly, BorderLayout.WEST);
    drawNorm.setSelected(true);

    drawNorm.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (drawNorm.isSelected()) {
          drawAllBlue.setSelected(false);
          drawAllRed.setSelected(false);
          drawBlueOnly.setSelected(false);
          drawRedOnly.setSelected(false);
          drawMode = "norm";
        } else {
          drawNorm.setSelected(true);
        }
      }
    });
    drawAllBlue.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (drawAllBlue.isSelected()) {
          drawNorm.setSelected(false);
          drawAllRed.setSelected(false);
          drawBlueOnly.setSelected(false);
          drawRedOnly.setSelected(false);
          drawMode = "allBlue";
        } else {
          drawAllBlue.setSelected(true);
        }
      }
    });
    drawAllRed.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (drawAllRed.isSelected()) {
          drawNorm.setSelected(false);
          drawAllBlue.setSelected(false);
          drawBlueOnly.setSelected(false);
          drawRedOnly.setSelected(false);
          drawMode = "allRed";
        } else {
          drawAllRed.setSelected(true);
        }
      }
    });
    drawBlueOnly.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (drawBlueOnly.isSelected()) {
          drawNorm.setSelected(false);
          drawAllBlue.setSelected(false);
          drawAllRed.setSelected(false);
          drawRedOnly.setSelected(false);
          drawMode = "blueMatches";
        } else {
          drawBlueOnly.setSelected(true);
        }
      }
    });
    drawRedOnly.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (drawRedOnly.isSelected()) {
          drawNorm.setSelected(false);
          drawAllBlue.setSelected(false);
          drawAllRed.setSelected(false);
          drawBlueOnly.setSelected(false);
          drawMode = "redMatches";
        } else {
          drawRedOnly.setSelected(true);
        }
      }
    });

    JLabel modeHead = new JLabel("Mode Selection");
    modeHead.setSize(100, 10);
    modeNorm.setSize(100, 10);
    modeHM.setSize(100, 10);
    modeMC.setSize(100, 10);
    modeAuto.setSize(100, 10);
    modeAlli.setSize(100, 10);

    modeSelection.add(modeHead, BorderLayout.NORTH);
    modeSelection.add(modeNorm, BorderLayout.WEST);
    modeSelection.add(modeHM, BorderLayout.WEST);
    modeSelection.add(modeMC, BorderLayout.WEST);
    modeSelection.add(modeAuto, BorderLayout.WEST);
    modeSelection.add(modeAlli, BorderLayout.WEST);

    modeNorm.setSelected(true);
    modeNorm.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (modeNorm.isSelected()) {
          modeHM.setSelected(false);
          modeMC.setSelected(false);
          colorMode = "norm";
        } else {
          modeNorm.setSelected(true);
        }
      }
    });
    modeHM.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (modeHM.isSelected()) {
          modeNorm.setSelected(false);
          modeMC.setSelected(false);
          colorMode = "heatMap";
        } else {
          modeHM.setSelected(true);
        }
      }
    });
    modeMC.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (modeMC.isSelected()) {
          modeHM.setSelected(false);
          modeNorm.setSelected(false);
          modeAlli.setSelected(false);
          alli = false;
          teamInputPanel.remove(teamNumTextField2);
          teamInputPanel.remove(teamNumTextField3);
          frame.pack();
          colorMode = "multiColor";
        } else {
          modeMC.setSelected(true);
        }
      }
    });
    modeAuto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (modeAuto.isSelected()) {
          auto = true;
        } else {
          modeAuto.setSelected(false);
          auto = false;
        }
      }
    });
    modeAlli.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (modeAlli.isSelected()) {
          alli = true;
          if (modeMC.isSelected()) {
            modeMC.setSelected(false);
            modeNorm.setSelected(true);
            colorMode = "norm";
          }
          teamInputPanel.add(teamNumTextField2);
          teamInputPanel.add(teamNumTextField3);
          checkBoxes.remove(checkBoxPanel);
          checkBoxes.remove(checkBoxOptions);
          frame.pack();
        } else {
          modeAlli.setSelected(false);
          alli = false;
          modeNorm.setSelected(true);
          colorMode = "norm";
          teamInputPanel.remove(teamNumTextField2);
          teamInputPanel.remove(teamNumTextField3);
          frame.pack();
        }
      }
    });

    grabMatches.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Grab Matches")) {
          checkBoxOptions.removeAll();
          JCheckBox addAllCheck = new JCheckBox("Select All");
          JCheckBox removeAllCheck = new JCheckBox("Deselect All");
          checkBoxesSelected.clear();
          if (!teamNumTextField.getText().equals("Team #")) {
            try {
              checkBoxPanel.removeAll();

              int teamNum = Integer.parseInt(teamNumTextField.getText());
              String[] data = curlTeamData(teamNum, eventKey);
              try {
                for (int i = 0; !data[i].contains("]"); i++) {
                  if (!data[i].contains("[")) {
                    String matchID = data[i];
                    matchID = matchID.replace(" ", "");
                    matchID = matchID.replace(" ", "");
                    matchID = matchID.replace("\"", "");
                    matchID = matchID.replace(",", "");
                    final String matchIDf = matchID;

                    String filename = "data/" + eventKey + "/" + matchID + ".txt";
                    File file = new File(filename);
                    if (file.exists()) {
                      JCheckBox checkBox = new JCheckBox((i) + ". " + matchID);
                      checkBox.setName(matchID);
                      checkBoxPanel.add(checkBox);
                      checkBox.setSelected(true);
                      checkBoxesSelected.add(checkBox);
                      try {
                        checkBoxList.add(checkBox);
                      } catch (NullPointerException npe) {
                        System.err.println(npe);
                      }
                      frame.pack();
                      checkBox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                          JCheckBox checkBox = (JCheckBox) e.getSource();
                          if (checkBox.isSelected()) {
                            try {
                              System.out.println("Selected " + matchIDf);
                              checkBoxesSelected.add(checkBox);
                              addAllCheck.setSelected(false);
                              removeAllCheck.setSelected(false);
                            } catch (Exception ex) {
                              System.err.println(ex);
                            }
                          } else {
                            try {
                              System.out.println("Deselected " + matchIDf);
                              checkBoxesSelected.remove(checkBox);
                              removeAllCheck.setSelected(false);
                            } catch (Exception ex) {
                              System.err.println(ex);
                            }
                          }
                        }
                      });
                    } else {
                      String[] match = { matchID };
                      getMatches(match);
                      if (file.exists()) {
                        JCheckBox checkBox = new JCheckBox((i) + ". " + matchID);
                        checkBox.setName(matchID);
                        checkBoxPanel.add(checkBox);
                        checkBox.setSelected(true);
                        checkBoxesSelected.add(checkBox);
                        try {
                          checkBoxList.add(checkBox);
                        } catch (NullPointerException npe) {
                          System.err.println(npe);
                        }
                        frame.pack();
                        checkBox.addActionListener(new ActionListener() {
                          public void actionPerformed(ActionEvent e) {
                            JCheckBox checkBox = (JCheckBox) e.getSource();
                            if (checkBox.isSelected()) {
                              try {
                                System.out.println("Selected " + matchIDf);
                                checkBoxesSelected.add(checkBox);
                                addAllCheck.setSelected(false);
                                removeAllCheck.setSelected(false);
                              } catch (Exception ex) {
                                System.err.println(ex);
                              }
                            } else {
                              try {
                                System.out.println("Deselected " + matchIDf);
                                checkBoxesSelected.remove(checkBox);
                                addAllCheck.setSelected(false);
                              } catch (Exception ex) {
                                System.err.println(ex);
                              }
                            }
                          }
                        });
                      }
                    }
                  }
                }
              } catch (ArrayIndexOutOfBoundsException aiobe) {
                System.err.println(aiobe);
              }
            } catch (NumberFormatException ex) {
              System.err.println(ex);
            }
          }
          addAllCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JCheckBox addAllCheck = (JCheckBox) e.getSource();
              if (addAllCheck.isSelected()) {
                removeAllCheck.setSelected(false);
                checkBoxesSelected.clear();
                System.out.println(checkBoxList.size());
                for (int j = 0; j < checkBoxList.size(); j++) {
                  JCheckBox checkbox = checkBoxList.get(j);
                  checkbox.setSelected(true);
                }
                checkBoxesSelected.addAll(checkBoxList);
              } else if (!addAllCheck.isSelected()) {
                removeAllCheck.setSelected(true);
                for (int j = 0; j < checkBoxList.size(); j++) {
                  JCheckBox checkbox = checkBoxList.get(j);
                  checkbox.setSelected(false);
                }
                checkBoxesSelected.clear();
              }
            }
          });
          removeAllCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JCheckBox removeAllCheck = (JCheckBox) e.getSource();
              if (removeAllCheck.isSelected()) {
                addAllCheck.setSelected(false);
                for (int j = 0; j < checkBoxList.size(); j++) {
                  JCheckBox checkbox = checkBoxList.get(j);
                  checkbox.setSelected(false);
                }
                checkBoxesSelected.clear();
              } else if (!removeAllCheck.isSelected()) {
                addAllCheck.setSelected(true);
                checkBoxesSelected.clear();
                for (int j = 0; j < checkBoxList.size(); j++) {
                  JCheckBox checkbox = checkBoxList.get(j);
                  checkbox.setSelected(true);
                }
                checkBoxesSelected.addAll(checkBoxList);
              }
            }
          });
          addAllCheck.setSelected(true);
          checkBoxOptions.add(addAllCheck);
          checkBoxOptions.add(removeAllCheck);
          alli = false;
          teamInputPanel.remove(teamNumTextField2);
          teamInputPanel.remove(teamNumTextField3);
          checkBoxes.add(checkBoxPanel, BorderLayout.WEST);
          checkBoxes.add(checkBoxOptions, BorderLayout.EAST);
          modeAlli.setSelected(false);
          frame.pack();
        }
      };
    });

    graphicsPanel = grabAndDraw(teamNumTextField, teamNumTextField2, teamNumTextField3, eventKey, frame, layeredPane,
        "null", null, "norm");

    draw.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Draw Image")) {
          try {
            eventKey = competitionIDField.getText();
            graphicsPanel = grabAndDraw(teamNumTextField, teamNumTextField2, teamNumTextField3, eventKey, frame,
                layeredPane, drawMode, checkBoxesSelected, colorMode);
          } catch (Exception ex) {
            System.err.println(ex);
          }
        }
      }
    });

    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Download Image")) {
          saveImage(graphicsPanel, teamNumTextField.getText(), teamNumTextField2.getText(),
              teamNumTextField3.getText());
        }
      }
    });

    bulkDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Bulk Download (Large Internet Usage)")) {
          panel.remove(bulkDownload);
          panel.add(bulkDownloadConfirm);
          frame.pack();
        }
      }
    });

    bulkDownloadConfirm.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Are you sure?")) {
          bulkDownload(eventKey);
        }
      }
    });

    ComponentListener componenetListener = new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent event) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      }

      @Override
      public void componentMoved(ComponentEvent e) {
      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {
      }
    };

    frame.addComponentListener(componenetListener);

    draw.setBounds(0, 25, 100, 40);
    draw.setAlignmentX(Component.LEFT_ALIGNMENT);
    draw.setAlignmentY(Component.BOTTOM_ALIGNMENT);

    teamInputPanel.add(teamNumTextField);

    JPanel info = new JPanel();
    info.add(info1);
    info.add(info2);
    info.add(info3);
    info.add(api);

    panel.add(teamNumLabel);
    panel.add(teamInputPanel);
    panel.add(eventKeyLabel);
    panel.add(competitionIDField);
    panel.add(draw);
    panel.add(save);
    panel.add(grabMatches);
    panel.add(bulkDownload);

    layeredPane.add(graphicsPanel, 0);
    container.add(layeredPane, BorderLayout.WEST);

    checkBoxes.add(checkBoxPanel, BorderLayout.WEST);
    checkBoxes.add(checkBoxOptions, BorderLayout.EAST);

    sideBar.add(panel, BorderLayout.CENTER);
    sideBar.add(modeSelection, BorderLayout.WEST);
    sideBar.add(modeOptions, BorderLayout.LINE_START);

    container.add(sideBar, BorderLayout.EAST);
    container.add(checkBoxes, BorderLayout.SOUTH);

    frame.add(container);
    frame.add(info, BorderLayout.PAGE_END);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.pack();
    frame.setVisible(true);

    return frame;
  }
}

class DrawPosition extends JPanel {
  private static final long serialVersionUID = 1L;
  private String[] xPos, yPos, matches;
  private String mode, colorMode;
  private JPanel checkBoxesPanel;
  private boolean auto;
  private boolean alli;
  private int maxData = 2147483647;

  public DrawPosition(String[] xPos, String[] yPos, JFrame frame, String mode, String colorMode, String[] matches,
      JPanel checkBoxesPanel, boolean auto, boolean alli) {
    this.xPos = xPos;
    this.yPos = yPos;
    this.mode = mode;
    this.colorMode = colorMode;
    this.matches = matches;
    this.checkBoxesPanel = checkBoxesPanel;
    this.auto = auto;
    this.alli = alli;
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
    g.clearRect(0, 0, 1200, 600);
    try {
      java.awt.Image img = ImageIO.read(new File("src/2020_field.png"));
      g.drawImage(img, 0, 0, null);
    } catch (IOException ex) {
      System.err.println(ex);
    }

    if (auto) {
      maxData = 150;
    }

    if (xPos == null) {
      return;
    }
    double scale = 22.5;
    int alliance = 0; // 0 is null, 1 is blue, 2 is red
    int j = 0;
    int colorIndex = -1;
    int index = 0;
    int dataPS = 1;
    Component[] components = checkBoxesPanel.getComponents();
    for (int k = 0; k < components.length; k++) {
      JCheckBox checkbox = (JCheckBox) components[k];
      checkbox.setBackground(Color.white);
      checkbox.setForeground(Color.black);
    }

    int[] colorR = { 0, 255, 0, 255, 51, 204, 102, 51, 102, 102, 255, 255, 0, 0, 0, 255, 153, 102, 51, 0, 204 };
    int[] colorG = { 0, 0, 0, 102, 204, 204, 102, 51, 51, 0, 255, 255, 102, 204, 0, 102, 0, 255, 153, 0, 0 };
    int[] colorB = { 0, 0, 255, 0, 255, 204, 102, 51, 0, 153, 255, 0, 0, 0, 204, 102, 0, 102, 255, 0, 0 };
    String[] fgColor = { "WHITE", "WHITE", "WHITE", "BLACK", "BLACK", "WHITE", "WHITE", "WHITE", "WHITE", "BLACK",
        "BLACK", "WHITE", "BLACK", "WHITE", "BLACK", "WHITE", "BLACK", "BLACK", "WHITE", "WHITE" };

    int imageCenterX = 700;
    int imageCenterY = 350;
    int transparency;
    int diameter = 1;
    colorIndex = -1;
    if (alli) {
      dataPS = 2;
    }
    for (int i = 0; i < xPos.length; i++) {
      if (isNumericDouble(xPos[i]) == false) {
        index = 0;
        alliance = 0;
        if (i < xPos.length - 1) {
          j = i + 1;
        }
        if (isNumericDouble(xPos[j]) == false) {
        } else if (((int) Math.round((Double.parseDouble(xPos[j])) * scale) - 5) > imageCenterX) {
          alliance = 2;
          colorIndex++;
        } else if (((int) Math.round((Double.parseDouble(xPos[j])) * scale) - 5) < imageCenterY) {
          alliance = 1;
          colorIndex++;
        }
      } else {
        index++;
        if ((index % dataPS) == 0) {
          if (index <= maxData) {
            if (colorMode.equals("multiColor")) {
              g.setColor(new Color(colorR[colorIndex], colorG[colorIndex], colorB[colorIndex]));
              try {
                for (int k = 0; k < components.length; k++) {
                  if (matches[colorIndex].equals(components[k].getName())) {
                    JCheckBox checkbox = (JCheckBox) components[k];
                    checkbox.setBackground(
                        new Color(colorR[colorIndex + 1], colorG[colorIndex + 1], colorB[colorIndex + 1]));
                    if (fgColor[colorIndex].equals("WHITE")) {
                      checkbox.setForeground(Color.WHITE);
                    } else {
                      checkbox.setForeground(Color.BLACK);
                    }
                  }
                }
              } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println(ex);
              }
              diameter = 5;
            } else if (colorMode.equals("norm")) {
              if (alli) {
                diameter = 6;
              } else {
                diameter = 6;
              }
              if (alliance == 1) {
                g.setColor(new Color(0, 0, 255));
              } else if (alliance == 2) {
                g.setColor(new Color(255, 0, 0));
              }
            } else if (colorMode.equals("heatMap")) {
              if (alli) {
                transparency = 2;
              } else {
                transparency = 4;
              }
              g.setColor(new Color(255, 0, 0, transparency));
              diameter = 25;
            }
            if (mode.equals("norm")) {
              try {
                int xCenter = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
                int yCenter = 600 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
                int x = xCenter - (diameter / 2);
                int y = yCenter - (diameter / 2);
                g.fillOval(x, y, diameter, diameter);
              } catch (NumberFormatException ex) {
                System.err.println(ex);
              }
            } else if (mode.equals("allRed")) {
              if (alliance == 1) {
                try {
                  int xCenter = (-(((int) Math.round((Double.parseDouble(xPos[i])) * scale)) - imageCenterX)
                      + imageCenterX) - 201;
                  int yCenter = (-((698 - (int) Math.round((Double.parseDouble(yPos[i])) * scale)) - imageCenterY)
                      + imageCenterY);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
                } catch (NumberFormatException ex) {
                  System.err.println(ex);
                }
              } else if (alliance == 2) {
                try {
                  int xCenter = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
                  int yCenter = 600 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
                } catch (NumberFormatException ex) {
                  System.err.println(ex);
                }
              }
            } else if (mode.equals("allBlue")) {
              if (alliance == 1) {
                try {
                  int xCenter = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
                  int yCenter = 600 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
                } catch (NumberFormatException ex) {
                  System.err.println(ex);
                }
              } else if (alliance == 2) {
                try {
                  int xCenter = (-(((int) Math.round((Double.parseDouble(xPos[i])) * scale)) - imageCenterX)
                      + imageCenterX) - 201;
                  int yCenter = (-((698 - (int) Math.round((Double.parseDouble(yPos[i])) * scale)) - imageCenterY)
                      + imageCenterY);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
                } catch (NumberFormatException ex) {
                  System.err.println(ex);
                }
              }
            } else if (mode.equals("blueMatches")) {
              if (alliance == 1) {
                try {
                  int xCenter = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
                  int yCenter = 600 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
                } catch (NumberFormatException ex) {
                  System.err.println(ex);
                }
              }
            } else if (mode.equals("redMatches")) {
              if (alliance == 2) {
                try {
                  int xCenter = (int) Math.round((Double.parseDouble(xPos[i])) * scale);
                  int yCenter = 600 - (int) Math.round((Double.parseDouble(yPos[i])) * scale);
                  int x = xCenter - (diameter / 2);
                  int y = yCenter - (diameter / 2);
                  g.fillOval(x, y, diameter, diameter);
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
    }
  }
}