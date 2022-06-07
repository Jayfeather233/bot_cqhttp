public class GetImage621 {
    static int retry=0;

    static StringBuilder dealInput(String input){
        String[] qs=input.split(" ");
        StringBuilder quest=new StringBuilder();
        for (String q : qs) {
            if (q.length() > 0) {
                quest.append('+').append(q);
            }
        }
        if(quest.length()==0) quest.append("eeveelution");
        if(!input.contains("id:")) {
            if (!input.contains("favcount") && !input.contains("score"))
                quest.append("+favcount:>400").append("+score:>200");
            if (!input.contains("order")) quest.append("+order:random");
            if (!input.contains("gore")) quest.append("+-gore");
            if (!input.contains("anthro")) quest.append("+-anthro");
            if (!input.contains("human")) quest.append("+-human");
        }
        System.out.println(quest);
        return quest;
    }

    static String GetImage(String input){
        if(input.equals(".default")){
            return "如未指定tag，默认加上eeveelution\n如未指定favcount或score，默认加上favcount:>400 score:>200\n如未指定以下tags，默认不搜索gore,anthro,human";
        }
        StringBuilder quest=dealInput(input);

        String answer;
        answer=HttpURLConnectionUtil.doGet("https://e621.net/posts?tags="+quest);
        int pos=answer.indexOf("data-id");
        pos=answer.indexOf("data-id",pos+1);
        int id=0;
        try {
            id = Integer.parseInt(answer.substring(pos + 9, answer.indexOf(' ', pos + 1) - 1));
        }catch(NumberFormatException e){
            return "None has been found.";
        }
        pos=answer.indexOf("data-fav-count");
        int fav_count=Integer.parseInt(answer.substring(pos+16,answer.indexOf(' ',pos)-1));

        answer=HttpURLConnectionUtil.doGet("https://e621.net/posts/"+id);

        pos=answer.indexOf("data-score");

        int score=Integer.parseInt(answer.substring(pos+12,answer.indexOf(' ',pos+1)-1));
        pos=answer.indexOf("contentUrl");
        pos=answer.indexOf("src",pos);
        String imageUrl=answer.substring(pos+5,answer.indexOf(' ',pos+1)-1);
        if(imageUrl.contains(".webm")|| !answer.contains("contentUrl")){
            retry++;
            if(retry>3){
                retry=0;
                return "Get .webm.\nUrl:https://e621.net/posts/"+id;
            }
            return GetImage(input);
        }

        quest=new StringBuilder();
        quest.append("[CQ:image,file=").append(imageUrl).append(",id=40000]\n");
        quest.append("Fav:").append(fav_count).append("  ");
        quest.append("Score:").append(score).append("\n");
        quest.append("id:").append(id);

        retry=0;
        return String.valueOf(quest);
    }
}
