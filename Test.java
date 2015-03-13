import java.util.*;

public class Test{

    public static void main(String[] args) throws Exception{

        Calendar cal = Calendar.getInstance();
        
        int i = 3;
        while (i-- > 0){
            System.out.println("" + cal.getTimeInMillis());
            Thread.sleep(2000);
        }
    }
}
