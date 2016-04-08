import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Umar on 28-Mar-16.
 */
public class LocalSpider {

    List<String> l;
    ConcurrentHashMap<String, List<String>> keywordIndex;
    ConcurrentHashMap<String, ConcurrentHashMap<String,String>> attributeIndex;
    public LocalSpider() {
        l = Collections.synchronizedList(new ArrayList<String>());
        keywordIndex= new ConcurrentHashMap<>();
        attributeIndex= new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        LocalSpider ls= new LocalSpider();

         ls.startcrawling("F:\\Movies");
        ls.startindexing();
        while (true) {
            System.out.println("Enter 1 for File name search, 2 for File Attributes");
            int opt= s.nextInt();
            switch (opt){
                case 1:
                    System.out.print("Enter your key word : ");
                    s.nextLine();
                    String q = s.nextLine();
//                    List<String> lv = ls.keywordIndex.get(q);
                    List<String> lv = ls.keywordIndex.keySet()
                            .stream()
                            .filter(ss -> ss.contains(q))
                            .collect(Collectors.toList());
                    if (lv != null) {
                        for (int o = 0; o < lv.size(); o++) {
                            List<String> cv=ls.keywordIndex.get(lv.get(o));
                            for(int g=0;g<cv.size();g++){
                                System.out.println(cv.get(g));
                            }
                        }
                    }
                    break;
                case 2:
                    System.out.print("Enter your file name: ");
                    s.nextLine();
                    String qq = s.nextLine();
                    List<String> lvv = ls.keywordIndex.get(qq);

                    if (lvv != null) {
                        for (int o = 0; o < lvv.size(); o++){
                            System.out.println(lvv.get(o));
                            ConcurrentHashMap<String,String> atr=ls.attributeIndex.get(lvv.get(o));
//                            System.out.println(atr.toString());
                            System.out.println("\tCreated: "+atr.get("created"));
                            System.out.println("\tModified: "+atr.get("modified"));
                            System.out.println("\tSize(Bytes): "+atr.get("sizebytes"));
                            System.out.println("\tOwner: "+atr.get("owner"));
                        }

                    }
                    break;

                default:

            }

        }
    }
    public  void startcrawling(String URL){
        File fr2 = new File(URL);
        File cnt1;

        if (fr2.exists() & fr2.isDirectory()) {

            File[] names = fr2.listFiles();                // storing all the files in the file array
            if (names == null) {                            // if directory is empty return from it

                return;
            }
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                cnt1 = names[i];
                if (cnt1.isDirectory()) {
                   Thread t = new Thread(new CrawlingThread(l,cnt1.getAbsolutePath()));
                    t.start();
                    threads.add(t);
                }
                else {
                    String g=cnt1.getAbsolutePath();
                    g = g.replace("\\", "/");
                    l.add(g);
                }

            }
            for (Thread thread : threads)
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            for( String s: l)
//                System.out.println(s);
        }
    }
    public  void startindexing(){
        int div= l.size()/3;
        List<Thread> threads = new ArrayList<>();
        Thread t = new Thread(new IndexingThread(l,0,div,keywordIndex,attributeIndex));
        t.start();
        threads.add(t);
        Thread t2 = new Thread(new IndexingThread(l,div,div*2,keywordIndex,attributeIndex));
        t2.start();
        threads.add(t2);
        Thread t3 = new Thread(new IndexingThread(l,div*2,div*3,keywordIndex,attributeIndex));
        t3.start();
        threads.add(t3);
        for (Thread thread : threads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
}
