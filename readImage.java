// ----------------------------------------------------------------------------------
// CSS 490 Multimedia Data Processing
// Project 1: readImage.java
// Lan Yang
// Created on: 04/03/2017
// Last update: 04/11/2017
// This project is implement a simple content-Based Image Retrieval system based on
// two different color histogram comparison method. This program implements the two
// different algorithms.
// ----------------------------------------------------------------------------------

import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;


public class readImage
{
  int imageCount = 1;
  int intensityBins [] = new int [26];
  int intensityMatrix [][] = new int[101][26];
  int colorCodeBins [] = new int [65];
  int colorCodeMatrix [][] = new int[101][65];

  /*Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
   * getColorCode methods are called.
  */
  public readImage()
  {
    while(imageCount < 101)
    {
      try
      {
        // the line that reads the image file
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("images/" + imageCount + ".jpg"));
        
        int width = image.getWidth();   // get the image width
        int height = image.getHeight(); // get the image height
        
        getIntensity(image, height, width); // get image's intensity
        getColorCode(image, height, width); // get image's color code
       
        imageCount++;   // update image number
      } 
      catch (IOException e)
      {
        System.out.println("Error occurred when reading the file.");
      }
    }
    
    writeIntensity();   // write image's intensity to intensity.txt
    writeColorCode();   // write image's color code to colorCodes.txt
    
  }
  
  //intensity method: calculate image's intensity
  public void getIntensity(BufferedImage image, int height, int width)
  {
    for(int i = 0; i < height; i++)
    {
        for(int j = 0; j < width; j++)
        {
            int pixel = image.getRGB(j, i); // get image's RGB
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;
            // calculate teh intensity
            double Intensity = 0.299*red + 0.587*green + 0.114*blue;
  
            int binIndex = 0;
            // set intensities to different bins
            if(Intensity < 240)
            {
                binIndex = (int)Intensity/10 + 1;
            }
            else
            {
                binIndex = 25;
            }
            
            intensityBins[binIndex]++;  // update the content number of bin in array
            intensityMatrix [imageCount][binIndex]++;   // update the content number of bin in Matrix
        }
    }
  }
  
  //color code method: calculate image's color code
  public void getColorCode(BufferedImage image, int height, int width)
  {
    for(int i = 0; i < height; i++)
    {
        for(int j = 0; j < width; j++)
        {
            int pixel = image.getRGB(j, i); // get image's RGB
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;
            
            byte redB = (byte) red;
            byte greenB = (byte) green;
            byte blueB = (byte) blue;
            // convert RGB numbers to 8 digits binary
            String redS = String.format("%8s", Integer.toBinaryString(redB & 0xff)).replace(' ', '0');
            String greenS = String.format("%8s", Integer.toBinaryString(greenB & 0xff)).replace(' ', '0');
            String blueS = String.format("%8s", Integer.toBinaryString(blueB & 0xff)).replace(' ', '0');
            // combine first to bits of RGB
            String Code = redS.substring(0,2) + greenS.substring(0,2) + blueS.substring(0,2);
            int colorCode = Integer.parseInt(Code,2);   // convert color code to integer
            
            int binIndex = colorCode + 1;
            colorCodeBins[binIndex]++;  // update the content number of bin in array
            colorCodeMatrix[imageCount][binIndex]++;    // update the content number of bin in Matrix
        }
    }
  }
  
  //This method writes the contents of the colorCode matrix to a file named colorCodes.txt.
  public void writeColorCode()
  {
    try
    {
        PrintWriter pw = new PrintWriter("colorCodes.txt"); // create a new text file
        for(int i = 1; i < 101; i++)
        {
            for(int j = 1; j < 65; j++)
            {
                pw.print(colorCodeMatrix[i][j] + " ");  // write color code into file
            }
            pw.println();   // next line
        }
        pw.close();
    }
    catch(FileNotFoundException EE)
    {
        System.out.println("The file colorCodes.txt cannot be found!!");
    }
  }
  
  //This method writes the contents of the intensity matrix to a file called intensity.txt
  public void writeIntensity()
  {
    try
    {
        PrintWriter pw = new PrintWriter("intensity.txt");  // create a new text file
        for(int i = 1; i < 101; i++)
        {
            for(int j = 1; j < 26; j++)
            {
                pw.print(intensityMatrix[i][j] + " ");  // write intensity into file
            }
            pw.println();   // next line
        }
        pw.close();
    }
    catch(FileNotFoundException EE)
    {
        System.out.println("The file intensity.txt cannot be found!!");
    }
  }
  
  public static void main(String[] args)
  {
    new readImage();
  }

}
