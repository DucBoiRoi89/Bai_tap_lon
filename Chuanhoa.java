import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Chuanhoa {
    public static String chuanhoaten(String s){
        String[] arr = s.toLowerCase().trim().split("\\s+");
        String res = "";
        for(String x : arr){
            res += Character.toUpperCase(x.charAt(0));
            for( int i = 1 ; i < x.length() ;  i ++ ){
                res += x.charAt(i);
            }
            res += " ";
        }
        return res;
    }
    public static String chuanhoangay(String s){
            String[] a = s.split("/");
            String res = String.format("%02d/%02d/%04d", Integer.parseInt(a[0]),Integer.parseInt(a[1]),Integer.parseInt(a[2]));
            return res;


    }

    public static void main(String[] args ){
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        String b = sc.nextLine();
        System.out.println(chuanhoaten(s));
        System.out.println(chuanhoangay(b));
        
        
    
    
}}
