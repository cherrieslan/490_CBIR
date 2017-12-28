// ----------------------------------------------------------------------------------
// CSS 490 Multimedia Data Processing
// Project 2: CBIR.java
// Lan Yang
// Created on: 04/20/2017
// Last update: 04/27/2017
// This project is to implement a Content-Based Image Retrieval system with relevance
// feedback option.
// ----------------------------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.*;

public class CBIR extends JFrame
{
    private JLabel photographLabel = new JLabel();  //container to hold a large 
    private JButton [] button; //creates an array of JButtons
    private int [] buttonOrder = new int [101]; //creates an array to keep up with the image order
    private double [] imageSize = new double[101]; //keeps up with the image sizes
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;
    private GridLayout gridLayout4;
    private JPanel panelBottom1;
    private JPanel panelBottom2;
    private JPanel panelTop;
    private JPanel buttonPanel;
    
    private JCheckBox relevanceFeedback;
    private JCheckBox [] select;    // creates an array of JCheckBoxs

    private double [][] intensityMatrix = new double [101][26];
    private double [][] colorCodeMatrix = new double [101][65];
    private double [][] intenColorMatrix = new double [101][90];
    private double [][] updatedMatrix1 = new double [101][90];
    private double [][] updatedMatrix2 = new double [101][90];

    private Map <Double , Integer> map;
    int picNo = 0;
    int imageCount = 1; //keeps up with the number of images displayed since the first page.
    int pageNo = 1;

    public static void main(String args[]) 
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                CBIR app = new CBIR();
                app.setVisible(true);
            }
        });
    }

    public CBIR() 
    {
        //The following lines set up the interface including the layout of the buttons and JPanels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Icon Demo: Please Select an Image");        
        panelBottom1 = new JPanel();
        panelBottom2 = new JPanel();
        panelTop = new JPanel();
        buttonPanel = new JPanel();
        gridLayout1 = new GridLayout(5, 4, 5, 5);
        gridLayout2 = new GridLayout(2, 1, 5, 5);
        gridLayout3 = new GridLayout(1, 2, 5, 5);
        gridLayout4 = new GridLayout(4, 2, 5, 5);
        
        setLayout(gridLayout3);
        panelBottom1.setLayout(gridLayout1);
        panelBottom2.setLayout(gridLayout1);
        panelTop.setLayout(gridLayout2);
        
        add(panelTop);
        add(panelBottom1);
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(gridLayout4);
        
        panelTop.add(photographLabel);
        panelTop.add(buttonPanel);
        add(panelBottom1);
        
        JButton previousPage = new JButton("Previous Page");
        JButton nextPage = new JButton("Next Page");
        JButton intensity = new JButton("Intensity");
        JButton colorCode = new JButton("Color Code");
        
        JButton intenColor = new JButton("Intensity & Color Code");
        relevanceFeedback = new JCheckBox("Relevance Feedback");
        relevanceFeedback.setEnabled(false);
        JButton reset = new JButton("Reset");
        JButton exit = new JButton("Exit");
        
        buttonPanel.add(previousPage);
        buttonPanel.add(nextPage);
        buttonPanel.add(intensity);
        buttonPanel.add(colorCode);
        
        buttonPanel.add(intenColor);
        buttonPanel.add(relevanceFeedback);
        buttonPanel.add(reset);
        buttonPanel.add(exit);
        
        intenColor.addActionListener(new intenColorHandler());
        relevanceFeedback.addItemListener(new relevanceFeedbackListener());
        reset.addActionListener(new resetHandler());
        exit.addActionListener(new exitHandler());
        
        nextPage.addActionListener(new nextPageHandler());
        previousPage.addActionListener(new previousPageHandler());
        intensity.addActionListener(new intensityHandler());
        colorCode.addActionListener(new colorCodeHandler());
        
        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[101];
        select = new JCheckBox[101];
        /*This for loop goes through the images in the database and stores them as icons and adds
         * the images to JButtons and then to the JButton array
        */
        for (int i = 1; i <= 100; i++) 
        {
                ImageIcon icon;
                icon = new ImageIcon(getClass().getResource("images/" + i + ".jpg"));
                Image img = icon.getImage() ;  
                Image newimg = img.getScaledInstance( icon.getIconWidth()/4, icon.getIconHeight()/4,  java.awt.Image.SCALE_DEFAULT ) ;  
                ImageIcon icon2 = new ImageIcon( newimg );
                String name = Integer.toString(i);
                if(icon != null)
                {
                    button[i] = new JButton(icon2);
                    select[i] = new JCheckBox(name);
                    select[i].setEnabled(false);
                    button[i].setLayout(new BorderLayout());
                    button[i].add(select[i],BorderLayout.PAGE_END);
                    select[i].setHorizontalAlignment(SwingConstants.CENTER);
                    select[i].setVerticalAlignment(SwingConstants.BOTTOM);
                    button[i].addActionListener(new IconButtonHandler(i, icon));
                    buttonOrder[i] = i;
                }
        }

        getImageSize(imageSize);
        readIntensityFile();
        readColorCodeFile();
        readIntenColorFile();
        displayFirstPage();
    }
    
    /*This method opens the intensity text file containing the intensity matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
    */
    public void readIntensityFile()
    {
      StringTokenizer token;
      Scanner read;
      int intensityBin = 0;
      String line = "";
      int lineNumber = 0;
         try
         {
           read =new Scanner(new File ("intensity.txt"));   // read intensity.txt
           
           for(lineNumber = 1; lineNumber <= 100; lineNumber++) // write intensity into matrix
           {
               for(intensityBin = 1; intensityBin <= 25; intensityBin++)
               {
                   intensityMatrix[lineNumber][intensityBin] = read.nextInt();
               }
           }
         }
         catch(FileNotFoundException EE)
         {
           System.out.println("The file intensity.txt does not exist");
         }
    }
    
    /*This method opens the color code text file containing the color code matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called colorCodeMatrix.
    */
    private void readColorCodeFile()
    {
      StringTokenizer token;
      Scanner read;
      int colorCodeBin = 0;
      String line = "";
      int lineNumber = 0;
         try{
           read =new Scanner(new File ("colorCodes.txt"));  // read colorCodes.txt
           
           for(lineNumber = 1; lineNumber <= 100; lineNumber++) // write colorCodes into matrix
           {
               for(colorCodeBin = 1; colorCodeBin <=64; colorCodeBin++)
               {
                   colorCodeMatrix[lineNumber][colorCodeBin] = read.nextInt();
               }
           }
         }
         catch(FileNotFoundException EE)
         {
           System.out.println("The file intensity.txt does not exist!!");
         }
    }
    
    //This method combines two matrixs of intensity and color code into one matrix which is called intenColorMatrix.
    private void readIntenColorFile()
    {
        int combinedBin = 0;
        int lineNumber = 0;
        for(lineNumber = 1; lineNumber<= 100; lineNumber++)
        {
            for(combinedBin = 1; combinedBin <= 89; combinedBin++)
            {
                if(combinedBin <= 25)
                {
                    intenColorMatrix[lineNumber][combinedBin] = intensityMatrix[lineNumber][combinedBin];
                }
                else
                {
                    intenColorMatrix[lineNumber][combinedBin] = colorCodeMatrix[lineNumber][combinedBin - 25];
                }
            }
        }
    }
    
    // Calculate manhattan distance
    private double manhattanDistance(double[] chosenImage, double[] compareImage, double chosenImageSize, double compareImageSize, int binNumbers)
    {
        double mD = 0.0;
        
        for(int i = 1; i < binNumbers; i++)
        {
            double chosen = (double) chosenImage[i] / chosenImageSize;
            double compare = (double) compareImage[i] / compareImageSize;
            mD += Math.abs(chosen - compare);
        }
        return mD;
    }
    
    // Calculate RF distance
    private double relevantDistance(double[] chosenImage, double[] compareImage, double chosenImageSize, double compareImageSize, double[] finalWeight)
    {
        double rD = 0.0;
        
        for(int j = 1; j < 90; j++)
        {
            double chosen = (double) chosenImage[j] / chosenImageSize;
            double compare = (double) compareImage[j] / compareImageSize;
            rD += finalWeight[j] * Math.abs(chosen - compare);
        }
        return rD;
    }
  
    // Calculate average values of each bin in matrix
    private double[] mean(double[][] relevantMatrix)
    {
        int row = relevantMatrix.length;
        double sum;
        double[] mean = new double [90];
        for(int j = 1; j < 90; j++)
        {
            sum = 0;
            for(int i = 1; i < row; i++)
            {
                sum += relevantMatrix[i][j];
            }
            mean[j] = sum / (row - 1);
        }
        return mean;
    }
    
    // Calculate standard deviation
    private double[] standardDeviation(double[][] relevantMatrix, double[] mean)
    {
        double sum;
        int row = relevantMatrix.length;
        double[] std = new double [90];
        for(int j = 1; j < 90; j++)
        {
            sum = 0;
            for(int i = 1; i < row; i++)
            {
                sum += Math.pow((relevantMatrix[i][j] - mean[j]),2);
            }
            std[j] = Math.sqrt(sum / (row-2));
        }
        return std;
    }
    
    // Normalizate the matrix
    private double [][] normalization(double[][] relevantMatrix, double[] mean, double[] std)
    {
        int row = relevantMatrix.length;
        double [][] normalizedMatrix = new double [row][90];
        for(int j = 1; j < 90; j++)
        {
            for(int i = 1; i < row; i++)
            {
                if(std[j] == 0.0 && mean[j] == 0.0)
                {
                    normalizedMatrix[i][j] = 0.0;
                }
                else if(!Double.isNaN( (relevantMatrix[i][j] - mean[j]) / std[j]))
                {
                    normalizedMatrix[i][j] = (relevantMatrix[i][j] - mean[j]) / std[j];
                }
            }
        }
        return normalizedMatrix;
    }
    
    // Calculate the normalized weight
    private double [] normalizedWeight(double[][] updatedMatrix)
    {
        double [] updatedWeight = new double [90];
        double [] normalizedWeight = new double [90];
        double [] average = mean(updatedMatrix);
        double [] std = standardDeviation(updatedMatrix, average); 
        double totalWeight = 0.0;
        double min = 0.0;
        double [] copy = new double [90];
        System.arraycopy(std, 0, copy, 0, 90);
        Arrays.sort(copy);

        for(int i = 0; i < 90; i++)
        {
            if(copy[i] != 0.0)
            {
                min = copy[i];
                break;
            }
        }
       
        for(int j = 1; j < 90; j++)
        {
            if(std[j] == 0.0 && average[j] != 0.0)
            {
                std[j] = min*0.5;
            }
        }
        
        for(int j = 1; j < 90; j++)
        {
            if(std[j] != 0.0)
            {
                updatedWeight[j] = 1/std[j];
            }
            else if(std[j] == 0.0 && average[j] != 0.0)
            {
                updatedWeight[j] = 1/std[j];
            }
            else if(std[j] == 0.0 && average[j] == 0.0)
            {
                updatedWeight[j] = 0.0;
            }
            totalWeight += updatedWeight[j];
        }
        
        for(int j = 1; j < 90; j++)
        {
            normalizedWeight[j] = updatedWeight[j] / totalWeight;
        }
        
        return normalizedWeight;
    }
    
    
    // Calculate image size
    private void getImageSize(double[] imageSize)
    {
        for(int imageCount = 1; imageCount <= 100; imageCount++)
        {
            try
            {
                // read images
                BufferedImage img = ImageIO.read(getClass().getResourceAsStream("images/" + imageCount + ".jpg"));
                int width = img.getWidth();
                int height = img.getHeight();
                imageSize[imageCount] = width*height;
            }
            catch (IOException e) 
            {
                System.out.println("Error occurred when reading the image file.");
            }    
        }   
    }
    
    
    /*This method displays the first twenty images in the panelBottom.  The for loop starts at number one and gets the image
     * number stored in the buttonOrder array and assigns the value to imageButNo.  The button associated with the image is 
     * then added to panelBottom1.  The for loop continues this process until twenty images are displayed in the panelBottom1
    */
    private void displayFirstPage()
    {
        int imageButNo = 0;
        panelBottom1.removeAll(); 
        for(int i = 1; i < 21; i++)
        {
            imageButNo = buttonOrder[i];
            panelBottom1.add(button[imageButNo]); 
            imageCount ++;
        }
        panelBottom1.revalidate();  
        panelBottom1.repaint();
    }
    
    /*This class implements an ActionListener for each iconButton.  When an icon button is clicked, the image on the 
     * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
    */ 
    private class IconButtonHandler implements ActionListener
    {
        int pNo = 0;
        ImageIcon iconUsed;
      
        IconButtonHandler(int i, ImageIcon j)
        {
            pNo = i;
            iconUsed = j;  //sets the icon to the one used in the buttons
        }
      
        public void actionPerformed( ActionEvent e)
        {
            photographLabel.setIcon(iconUsed);
            picNo = pNo;
        }
    }
    
    /*This class implements an ActionListener for the nextPageButton.  The last image number to be displayed is set to the 
     * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
     * images because there are only 100 images to be displayed.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
    */
    private class nextPageHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            int imageButNo = 0;
            int endImage = imageCount + 20;
            if(endImage <= 101)
            {
                panelBottom1.removeAll(); 
                for (int i = imageCount; i < endImage; i++) 
                {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount++;
                }
                panelBottom1.revalidate();  
                panelBottom1.repaint();
            }
        }
    }
    
    /*This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the 
     * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
     * images because the starting image is 1.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
    */
    private class previousPageHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            int imageButNo = 0;
            int startImage = imageCount - 40;
            int endImage = imageCount - 20;
            if(startImage >= 1)
            {
                panelBottom1.removeAll();
                /*The for loop goes through the buttonOrder array starting with the startImage value
                 * and retrieves the image at that place and then adds the button to the panelBottom1.
                 */
                for (int i = startImage; i < endImage; i++) 
                {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount--;
                }
                panelBottom1.revalidate();  
                panelBottom1.repaint();
            }
        }
    }
    
    /*This class implements an ActionListener when the user selects the intensityHandler button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one.
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    private class intensityHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        { 
            relevanceFeedback.setSelected(false);
            relevanceFeedback.setEnabled(false);
            if(picNo == 0)
            {
                return;
            }
            
            double [] distance = new double [101];
            map = new HashMap<Double, Integer>(); // for store distance
            double picSize = imageSize[picNo];
          
            // Calculate manhattan distance
            for(int i = 1; i <= 100; i++)
            {
                distance[i]= manhattanDistance(intensityMatrix[picNo], intensityMatrix[i], picSize, imageSize[i], 26);
                map.put(distance[i], i);  // store manhattan distance into hash map
            }
          
            Arrays.sort(distance);    // sort the distance
          
            for(int i = 1; i <= 100; i++)
            {
                buttonOrder[i] = map.get(distance[i]);              
            }
          
            imageCount = 1;
            displayFirstPage();
            panelBottom1.revalidate();  
            panelBottom1.repaint();
        }
    }
    
    /*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */ 
    private class colorCodeHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            relevanceFeedback.setSelected(false);
            relevanceFeedback.setEnabled(false);
            if(picNo == 0)
            {
                return;
            }
          
            double [] distance = new double [101];
            map = new HashMap<Double, Integer>(); // for store distance
            double picSize = imageSize[picNo];
          
            // Calculate manhattan distance
            for(int i = 1; i <= 100; i++)
            {
                distance[i] = manhattanDistance(colorCodeMatrix[picNo], colorCodeMatrix[i], picSize, imageSize[i], 65);
                map.put(distance[i], i);
            }
          
            Arrays.sort(distance);    // sort distance
          
            for(int i = 1; i <= 100; i++)
            {
                buttonOrder[i] = map.get(distance[i]);  
            }
          
            imageCount = 1;
            displayFirstPage();
            panelBottom1.revalidate();  
            panelBottom1.repaint();
        }
    }

    /*This class implements an ActionListener when the user selects the intenColor button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intenColorMatrix starts with zero and not one. 
     * The size of the image is retrieved from the imageSize array.  The selected image's intenColor bin values are 
     * compared to all the other image's intenColor bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     * After the result, user can provide relevance feedback to improve the result by clilked the JCheckBox of each image.
     * The Intensity&ColorCode button give the simliarity result with relevance feedbacks.
     */     
    private class intenColorHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            if(picNo == 0)
            {
              return;
            }
                
            relevanceFeedback.setEnabled(true);
            double picSize = imageSize[picNo];
            double [] distance = new double [101];
            map = new HashMap<Double, Integer>(); // for store distance
            double [] average = new double [90];
            double [] sd = new double [90];
         
            if(!relevanceFeedback.isSelected())
            {
                double [][] temp1 = new double [101][90];
                for(int i = 1; i <= 100; i++)
                {    
                    for(int j = 1; j <= 89; j++)
                    {   
                        temp1[i][j] = intenColorMatrix[i][j] / imageSize[i];
                    }
                }
                
                average = mean(temp1);
                sd = standardDeviation(temp1, average);
                updatedMatrix1 = normalization(temp1, average, sd);
            
                // Calculate manhattan distance
                for(int i = 1; i <= 100; i++)
                {
                    distance[i]= manhattanDistance(updatedMatrix1[picNo], updatedMatrix1[i], picSize, imageSize[i], 90);
                    map.put(distance[i], i);  // store manhattan distance into hash map
                }
          
                Arrays.sort(distance);    // sort the distance
          
                for(int i = 1; i <= 100; i++)
                {
                    buttonOrder[i] = map.get(distance[i]);              
                }
            }
            else if(relevanceFeedback.isSelected())
            {
                int count = 0;
                double [][] relevanceMatrix = new double [101][90];
                
                for(int i = 1; i <= 100; i++)
                {
                    if(select[i].isSelected())
                    {
                        count++;
                        System.arraycopy(updatedMatrix1[i], 0, relevanceMatrix[count], 0, 90);
                    }
                }
                
                if(count == 0)
                {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame, "Need Relevance Feedback!!");
                    return;
                }
                
                if(count == 1)
                {
                    for(int i = 1; i <= 100; i++)
                    {
                        if(select[i].isSelected())
                        {
                            int one = i;
                            if(one == picNo)
                            {
                                return;
                            }
                            else
                            {
                                JFrame frame = new JFrame();
                                JOptionPane.showMessageDialog(frame, "The query image should be first Relevance Feedback!!");
                                return;
                            }
                        }
                    }
                }
                
                if(count > 1)
                {
                    boolean check = false;
                    for(int i = 1; i <= 100; i++)
                    {
                        if(select[i].isSelected())
                        {
                            int one = i;
                            if(one == picNo)
                            {
                                check = true;
                            }
                        }
                    }
                    if(check == false)
                    {
                        JFrame frame = new JFrame();
                        JOptionPane.showMessageDialog(frame, "The query image should be first Relevance Feedback!!");
                        return;
                    }
                }
                
                double [][] temp2 = new double[count+1][90];
                
                for(int i = 1; i <= count; i++)
                {
                    for(int j = 1; j <= 89; j++)
                    {
                        temp2[i][j] = relevanceMatrix[i][j];    // put the selected images into a matrix
                    }
                }
               
                double[] finalWeight = normalizedWeight(temp2); // calculate the normalized weight
                
                // Calculate relevant distance
                for(int i = 1; i <= 100; i++)
                {
                    distance[i]= relevantDistance(updatedMatrix1[picNo], updatedMatrix1[i], picSize, imageSize[i], finalWeight);
                    map.put(distance[i], i);
                }
                
                Arrays.sort(distance);    // sort the distance
          
                for(int i = 1; i <= 100; i++)
                {
                    buttonOrder[i] = map.get(distance[i]);              
                }
            }
            
            imageCount = 1;
            displayFirstPage();
            panelBottom1.revalidate();  
            panelBottom1.repaint();
        }
    }
    
    // Check if the Relevance Feedback checkbox is select, enable all the JCheckBoxes of the images. Otherwise, disable them.
    private class relevanceFeedbackListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                for(int i = 1; i <= 100; i++)
                {
                    select[i].setEnabled(true);
                }
            }
            else
            {
                for(int i = 1; i <= 100; i++)
                {
                    select[i].setSelected(false);
                    select[i].setEnabled(false);
                }
            }
        }
    }
    
    // Listen exit button, if is is clicked, quit the program.
    private class exitHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            System.exit(0);
        }
    }

    // Listen reset button, if it is clicked, restart the program.
    private class resetHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            dispose();
            CBIR app = new CBIR();
            app.setVisible(true);
        }
    }
}
