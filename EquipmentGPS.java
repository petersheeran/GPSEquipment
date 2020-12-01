import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class EquipmentGPS {
    public static int holdingNext = Integer.MAX_VALUE;
    public static boolean farAway = false;
    public static double minDist2 = Integer.MAX_VALUE;
    //Result string starts at first location
    public static String result = "0,";
    public static int closest1;
    public static double minDistance = Integer.MAX_VALUE;
    public static int[] unused = new int [1001];
    public static double[][] coords = new double[1001][2];
    //Current location when starting is the first in the spreadsheet of locations
    public static int currentLocation = 0;
    public static int closest;

    public static void main(String args[] ) throws Exception {
        File gps = new File("D:\\Users\\Peter\\Downloads\\EquipmentGPSCo-ordinates.xlsx");
        FileInputStream input = new FileInputStream(gps);
        XSSFWorkbook workbook = new XSSFWorkbook(input);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIt = sheet.iterator();

        for(int i = 0; i < 1001; i++){
            Row row = rowIt.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            //Adding every location to an array to track what locations haven't been visited
            unused[i] = i;

            for(int j = 0; j<2; j++){
                coords[i][j] = Double.parseDouble(cellIterator.next().toString());
            }
        }
        //Integer.MAX_VALUE is the value I'm giving to every visited location, removing the first location from the available valid locations
        unused[0] = Integer.MAX_VALUE;
        //System.out.println(coords[0][0] + "," + coords[0][1]);
        //Call method to find the closest valid location
        findClosest();
        //System.out.println(coords[0][0] + "," + coords[0][1]);
        result = result.concat("0");
        System.out.println(result);

        workbook.close();
        input.close();
    }

    public static double distance(double lat1, double long1, double lat2, double long2){
        double radius = 6371;
        //Distance formula using latitude and longitude
        return radius * Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(long1) - Math.toRadians(long2)));

    }

    public static String findClosest(){
        //Loop to find the closest valid location
        for (int k = 0; k < 1001; k++) {
            if (k != currentLocation && distance(coords[currentLocation][0], coords[currentLocation][1], coords[k][0], coords[k][1]) > 100 && unused[k] != Integer.MAX_VALUE) {
                if (distance(coords[currentLocation][0], coords[currentLocation][1], coords[k][0], coords[k][1]) < minDistance) {
                    minDistance = distance(coords[currentLocation][0], coords[currentLocation][1], coords[k][0], coords[k][1]);
                    closest = k;
                }
            }
            //If there are no unvisited locations more than 100km away, this finds the nearest valid location regardless of whether it was visited or not
            else if (k == 1000 && minDistance == Integer.MAX_VALUE) {
                for (int l = 0; l < 1001; l++) {
                    if (k != currentLocation && distance(coords[currentLocation][0], coords[currentLocation][1], coords[l][0], coords[l][1]) > 100) {
                        if (distance(coords[currentLocation][0], coords[currentLocation][1], coords[l][0], coords[l][1]) < minDistance) {
                            minDistance = distance(coords[currentLocation][0], coords[currentLocation][1], coords[l][0], coords[l][1]);
                            closest = l;
                        }
                    }
                }
            }
        }
        //If the nearest valid location is over a certain distance away (510km worked best), it will check if there are any unvisited locations within 100km of the current location
        //537
        if (distance(coords[currentLocation][0], coords[currentLocation][1], coords[closest][0], coords[closest][1]) > 537) {
            //Loop to find locations <100km away
            close: for (int m = 0; m < 1001; m++) {
                if (m != currentLocation && distance(coords[currentLocation][0], coords[currentLocation][1], coords[m][0], coords[m][1]) < 100 && unused[m] != Integer.MAX_VALUE) {
                    //If an unvisited location is <100km away, the number of that location is held by the holdingNext variable
                    holdingNext = m;
                    //Loop to find the nearest location over 100km away from the current location and the location holdingNext
                    for (int n = 0; n < 1001; n++) {
                        if (distance(coords[n][0], coords[n][1], coords[holdingNext][0], coords[holdingNext][1]) >= 100) {
                            if (n != currentLocation && distance(coords[currentLocation][0], coords[currentLocation][1], coords[n][0], coords[n][1]) >= 100) {
                                if (distance(coords[currentLocation][0], coords[currentLocation][1], coords[n][0], coords[n][1]) < minDistance) {
                                    minDistance = distance(coords[currentLocation][0], coords[currentLocation][1], coords[n][0], coords[n][1]);
                                    closest1 = n;
                                    //The farAway boolean is changed to true to say that the nearest valid unvisited was too far away, so the route is going somewhere else instead
                                    farAway = true;
                                }
                            }
                        }
                    }
                    //Once somewhere valid has been found, the loop is broken out of
                    if (holdingNext != Integer.MAX_VALUE) {
                        break close;
                    }
                }
            }
        }

        if (!farAway) {
            //Next leg of the journey is completed
            currentLocation = closest;
            //Change value in the unused array to show the location has been visited
            unused[currentLocation] = Integer.MAX_VALUE;
            //Reset the lowest distance
            minDistance = Integer.MAX_VALUE;
            //Add the location to the result string
            result = result.concat(currentLocation + ",");
            //System.out.println(coords[currentLocation][0] + "," + coords[currentLocation][1]);
        }
        //In the case of the next valid location being too far away
        if (farAway) {
            //Next leg of the journey is completed to an already visited location first
            currentLocation = closest1;
            //Reset values
            closest1 = Integer.MAX_VALUE;
            minDistance = Integer.MAX_VALUE;
            //Add the location to the result string
            result = result.concat(currentLocation + ",");
            //System.out.println(coords[currentLocation][0] + "," + coords[currentLocation][1]);

            //Now the unvisited location is visited
            currentLocation = holdingNext;
            //Change value in the unused array to show the location has been visited
            unused[currentLocation] = Integer.MAX_VALUE;
            //Reset minDist for the close loop
            minDist2 = Integer.MAX_VALUE;
            //Add the location to the result string
            result = result.concat(currentLocation + ",");
            //System.out.println(coords[currentLocation][0] + "," + coords[currentLocation][1]);
            //Change farAway boolean to false for next leg of the journey
            farAway = false;
        }
        //While there are still unvisited locations, this method will run recursively
        for (int l = 0; l < 1001; l++) {
            if (unused[l] != Integer.MAX_VALUE) {
                findClosest();
            }
        }
        //Returns result string
        return result;
    }
}