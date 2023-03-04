package dev.jlkesh.java_telegram_bots.dataserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {





    public static void main (String[] args) {
        // keep this function call here
//        Scanner s = new Scanner(System.in);
//        System.out.println(FindIntersection(new String[] {"1, 3, 4, 7, 13", "1, 2, 4, 13, 15"}));
//        System.out.print("");

//        anagram("run", "urn");

        CountingAnagrams("a c b c run urn urn");
    }
    public static int firstOccuranceIndex(String[] arr, String s){
        for(int i=0; i<arr.length; i++){
            if(arr[i].equals(s)){
                return i;
            }
        }

        return -1;
    }
    public static String sort(String s){
        char[] arr=new char[s.length()];
        for(int i=0; i<s.length(); i++){
            arr[i]=s.charAt(i);
        }
        Arrays.sort(arr);
        return String.valueOf(arr);
    }
    public static String CountingAnagrams(String str) {
        int counter=0;

        String[] arr=str.split(" ");

        for(int i=0; i<arr.length; i++){
            for(int j=i+1; j<arr.length; j++){
                if(arr[i].length()!=arr[j].length()
                        || arr[i].length()==1 ||
                        arr[j].length()==1){
                    continue;
                }
                if(j!=firstOccuranceIndex(arr, arr[j])){
                    continue;
                }
                if(!arr[i].equals(arr[j])){

                    String s1=sort(arr[i]);
                    String s2=sort(arr[j]);
                    if(s1.equals(s2)){
                        counter++;
                    }
                }
            }
        }
        return ""+counter;
    }

    public static boolean anagram(String s1, String s2){
        char[] a={'u', 'r', 'n'};
        Arrays.sort(a);
        System.out.println("a = " + Arrays.toString(a));
        String b= String.valueOf(a);
        System.out.println("b = " + b);

        List<String> l=new ArrayList<>();

//        l.size()

        return true;
    }
}
