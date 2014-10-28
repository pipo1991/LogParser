import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;


public class ParseLog {
    
     static String[] urls={
         "GET /api/users/{user_id}/count_pending_messages",
         "GET /api/users/{user_id}/get_messages",
         "GET /api/users/{user_id}/get_friends_progress",
         "GET /api/users/{user_id}/get_friends_score",
         "POST /api/users/{user_id}",
         "GET /api/users/{user_id}"
     };

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //You should put the log file in the same folder of this class.
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("sample.log")));
        String nextLine="";
        int[]numberOfTimesUrlIsCalled=new int[6];
        LinkedList<String> []dynoResponding=new LinkedList[6];
        LinkedList<Long> []responseTime=new LinkedList[6];
        for(int i=0;i<6;i++){
            dynoResponding[i]=new LinkedList<String>();
            responseTime[i]=new LinkedList<Long>();
        }
        while((nextLine=br.readLine())!=null){
            StringTokenizer logLine=new StringTokenizer(nextLine);
            discardTokens(logLine, 3);          
            String method=logLine.nextToken().split("=")[1];
            String path=logLine.nextToken().split("=/")[1];
            int indexOfThePath=urlParse(path, method);
            if(indexOfThePath==-1){
                continue;
            }
            numberOfTimesUrlIsCalled[indexOfThePath]++;
            discardTokens(logLine, 2);  
            String dyno=logLine.nextToken().split("=")[1];
            dynoResponding[indexOfThePath].add(dyno);
            String connectTime=logLine.nextToken();
            connectTime=connectTime.split("=")[1];
            connectTime=connectTime.substring(0, connectTime.length()-2);
            String serviceTime=logLine.nextToken();
            serviceTime=serviceTime.split("=")[1];
            serviceTime=serviceTime.substring(0, serviceTime.length()-2);
            long respondTime=Long.parseLong(connectTime)+Long.parseLong(serviceTime);
            responseTime[indexOfThePath].add(respondTime);
        }
		//output file 
        PrintStream ps=new PrintStream(new File("result.log"));
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<6;i++){
            if(i>0)
                sb.append("\n");
            sb.append(urls[i]).append(":").append("\n");
            sb.append("Number of Times This URL is called=")
                    .append(numberOfTimesUrlIsCalled[i]).append(" times.\n");
            LinkedList<Long> responseTimeOfThisURL=responseTime[i];
            //Time Complexity= O(n Log n)
            Collections.sort(responseTimeOfThisURL);
            sb.append("Mean Of Response time=").append(getAverageOfResponseTime(responseTimeOfThisURL))
                    .append(" ms.\n");
            sb.append("Median Of Response time=").append(getMedianOfResponseTime(responseTimeOfThisURL))
                    .append(" ms.\n");
            sb.append("Mode Of Response time=").append(getModeOfResponseTime(responseTimeOfThisURL))
                    .append(" times.\n");
            sb.append("Dyno responding the most");
            LinkedList<String> dynosRespondingToThisURL=dynoResponding[i];
            sb.append("The most responding Dyno=").append(getTheMostRespondingDyno(dynosRespondingToThisURL))
                    .append("\n");
        }
        ps.append(sb);
        ps.close();
    }
    
    public static void discardTokens(StringTokenizer st,int numberOfDiscardedTokens){
        for(int i=0;i<numberOfDiscardedTokens;i++){
            st.nextToken();
        }
    }
    
    /*
     * This method 
     * return 0 GET /api/users/{user_id}/count_pending_messages 
     * return 1 GET /api/users/{user_id}/get_messages 
     * return 2 GET /api/users/{user_id}/get_friends_progress 
     * return 3 GET /api/users/{user_id}/get_friends_score 
     * return 4 POST /api/users/{user_id} 
     * return 5 GET /api/users/{user_id}
     * return -1 for other links
     * Supposing that GET /api/users/{user_id}/..../.... doesn't belong to GET /api/users/{user_id}
     */
    public static int urlParse(String path,String method){
        String[] urlParts=path.split("/");
        if(urlParts.length<3){
            return -1;
        }
        if(urlParts[0].equals("api")&&urlParts[1].equals("users")){
            if(isNumber(urlParts[2])){
                if(urlParts.length==3){
                   if(method.equals("GET")){
                       return 5;
                   }
                   if(method.equals("POST")){
                       return 4;
                   }
                }
                if(!method.equals("GET")){
                   return -1;
                }
                if(urlParts[3].equals("count_pending_messages")){
                   return 0;
                }
                if(urlParts[3].equals("get_messages")){
                   return 1;
                }
                if(urlParts[3].equals("get_friends_progress")){
                   return 2;
                }
                if(urlParts[3].equals("get_friends_score")){
                   return 3;
                }
            }
        }
        return -1;
    }

    static boolean isNumber(String urlPart){
        for(int i=0;i<urlPart.length();i++){
            if(urlPart.charAt(i)<'0'|| urlPart.charAt(i)>'9'){
                return false;
            }
        }
        return true;
    }
    
    /*
     * Time Complexity= O(n)
     */
    static String getAverageOfResponseTime(LinkedList<Long> responseTimeOfThisURL){
        if(responseTimeOfThisURL.size()<1){
            return "0";
        }
        Iterator<Long> it=responseTimeOfThisURL.iterator();
        long sum=0;
        while(it.hasNext()){
            sum+=it.next();
        }
        double result=sum/responseTimeOfThisURL.size();
        return String.format("%.2f", result);
    }
    
    static String getMedianOfResponseTime(LinkedList<Long> responseTimeOfThisURL){
        if(responseTimeOfThisURL.size()<1){
            return "0";
        }
        int n=responseTimeOfThisURL.size();
        Iterator<Long> it=responseTimeOfThisURL.iterator();
        if(n%2!=0){
            int median=n/2;
            for(int i=0;i<median;i++){
                it.next();
            }
            return it.next()+"";
        }else{
            int median1=(n/2)-1;
            for(int i=0;i<median1;i++){
                it.next();
            }
            double result=(it.next()+it.next())/2.0;
            return String.format("%.2f", result);
        }
    }
    
    /*
     * WE suppose in case of Mutiple response 
     * that happened same number of time we get the first one
     * Time Complexity= O(n)
     */
    static String getModeOfResponseTime(LinkedList<Long> responseTimeOfThisURL){
        if(responseTimeOfThisURL.size()<1){
            return "0";
        }
        Iterator<Long> it=responseTimeOfThisURL.iterator();
        int maxTimes=1;
        long maxNumber=it.next();
        int counter=1;
        long last=maxNumber;
        while(it.hasNext()){
            long thisResponse=it.next();
            if(thisResponse==last){
                counter++;
                if(counter>maxTimes){
                    maxNumber=thisResponse;
                    maxTimes=counter;
                }
            }else{
                last=thisResponse;
                counter=1;
            }
        }
        return maxNumber+" ms has happened "+maxTimes;
    }
    
    /*
     * WE suppose in case of Mutiple dynos 
     * that have same number of times we get the first one
     * Takes of O(n)
     */
    static String getTheMostRespondingDyno(LinkedList<String> dynosRespondingToThisURL){
        if(dynosRespondingToThisURL.size()<1){
            return "0";
        }
        HashMap<String,Integer> hm=new HashMap<String, Integer>();
        Iterator<String> it=dynosRespondingToThisURL.iterator();
        String maxDyno=it.next();
        int max=1;
        while(it.hasNext()){
            String nextDyno=it.next();
            if(hm.containsKey(nextDyno)){
                int numberOfTimesResponded=hm.get(nextDyno)+1;
                hm.put(nextDyno, numberOfTimesResponded);
                if(numberOfTimesResponded>max){
                    max=numberOfTimesResponded;
                    maxDyno=nextDyno;
                }
            }else{
               hm.put(nextDyno, 1); 
            }
        }
        return maxDyno;
    }
    
}
