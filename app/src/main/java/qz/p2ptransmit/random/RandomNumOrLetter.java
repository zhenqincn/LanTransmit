package qz.p2ptransmit.random;

import java.util.Random;

/**
 * Created by QinZhen on 2017/2/13.
 */

public class RandomNumOrLetter {
    private static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String numberChar = "0123456789";

    public static String createRandNumOrLetter(int lenth)
    {
        Random random = new Random();
        char[] randChar = new char[lenth];
        for(int i = 0; i< lenth; i++)
        {
            randChar[i] = allChar.charAt(random.nextInt(62));
        }
        return String.valueOf(randChar);
    }


    public static String createRandNum(int lenth)
    {
        Random random = new Random();
        char[] randChar = new char[lenth];
        for(int i = 0; i< lenth; i++)
        {
            randChar[i] = numberChar.charAt(random.nextInt(10));
        }
        return String.valueOf(randChar);
    }


    public static String createRandLetter(int lenth)
    {
        Random random = new Random();
        char[] randChar = new char[lenth];
        for(int i = 0; i< lenth; i++)
        {
            randChar[i] = letterChar.charAt(random.nextInt(52));
        }
        return String.valueOf(randChar);
    }
}
