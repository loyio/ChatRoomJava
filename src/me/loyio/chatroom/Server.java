package me.loyio.chatroom;

/**
 * Project: ChatRoom
 * Package: me.loyio.chatroom
 * User: loyio
 * Date: 6/11/21
 */
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
public class Server{
    public static void main(String args[]){
        ServerSocket server=null;
        Socket you=null;
        Map<String,ServerThread> peopleList=null;
        peopleList=new HashMap<String, ServerThread>();
        while(true){
            try  {
                server=new ServerSocket(4331);
            }
            catch(IOException e1){
                System.out.println("正在监听");
            }
            try  {
                you=server.accept();
                InetAddress address=you.getInetAddress();
                System.out.println("客户的IP:"+address);
            }
            catch (IOException e) {}
            if(you!=null){
                ServerThread peopleThread=new ServerThread(you,peopleList);
                peopleThread.start();
            }
            else continue;
        }
    }
}
class ServerThread extends Thread{
    String name=null;
    Socket socket=null;
    File file=null;
    DataOutputStream out=null;
    DataInputStream  in=null;
    Map<String,ServerThread> peopleList=null;
    ServerThread(Socket t, Map<String,ServerThread> list){
        peopleList=list;
        socket=t;
        try {  in=new DataInputStream(socket.getInputStream());
            out=new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e) {}
    }
    public void run(){
        Connection conn;
        Statement stmt=null;
        ResultSet rs;
        String driverName = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/chat_room_db?useUnicode=true&characterEncoding=utf-8";
        String userName = "root";
        String password = "123456";
        try {
            Class.forName(driverName);
            conn =  DriverManager.getConnection(url, userName, password);
            if(conn == null){
                System.out.println("Conn is null point");
            }
            stmt=conn.createStatement();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Boolean success=false;
        while(true){
            String s,pw=null;
            try{
                s=in.readUTF();
                if(s.startsWith("姓名:")){
                    name=s.substring(s.indexOf(":")+1, s.indexOf("#"));
                    pw=s.substring(s.indexOf("#")+1);
                    try {
                        rs=stmt.executeQuery("select * from user where name='"+name+"' and pw='"+pw+"'");
                        boolean boo=peopleList.containsKey(name);
                        if(boo!=false) {
                            out.writeUTF("用户已登录！");
                            System.out.println("用户已登录！");
                        }else if(rs.next()){
                            System.out.println("登陆");
                            success=true;
                            peopleList.put(name,this);
                            Collection<ServerThread> values=peopleList.values();
                            Iterator<ServerThread> chatPersonList=values.iterator();

                            while(chatPersonList.hasNext()) {
                                ServerThread chatPerson = (ServerThread)chatPersonList.next();
                                chatPerson.out.writeUTF("欢迎" + name + "上线");
                                chatPerson.out.writeUTF("user_online:" + this.peopleList.toString());
                            }
                        }
                        else{
                            out.writeUTF("用户名或密码不对，请重新登录");

                        }
                    }  catch (SQLException e) {
                        e.printStackTrace();
                    }


                }else if(s.startsWith("注册:")){
                    name=s.substring(s.indexOf(":")+1, s.indexOf("#"));
                    pw=s.substring(s.indexOf("#")+1);
                    try {
                        int result=stmt.executeUpdate("insert into user(name,pw) values('"+name+"' ,'"+pw+"')");
                        if(result>0)
                            success=true;
                        else
                            out.writeUTF("用户名重复，请重新注册");
                    } catch (SQLException e) {
                    }


                }
                else if(s.startsWith("聊天内容")&& success){
                    String message=s.substring(s.indexOf(":")+1);
                    Collection<ServerThread> values=peopleList.values();
                    Iterator<ServerThread> chatPersonList=values.iterator();
                    while(chatPersonList.hasNext()){
                        ((ServerThread)chatPersonList.next()).out.writeUTF (message);
                    }
                }
            }
            catch(IOException ee){
                Collection<ServerThread> values=peopleList.values();
                Iterator<ServerThread> chatPersonList=values.iterator();
                while(chatPersonList.hasNext()){
                    try {  ServerThread  th=(ServerThread)chatPersonList.next();
                        if(th!=this&&th.isAlive())
                            th.out.writeUTF(name+"离开了");
                    }
                    catch(IOException eee){}
                }
                peopleList.remove(name);
                try { socket.close();
                }
                catch(IOException eee){}
                System.out.println(name+"离开了");
                break;
            }
        }
    }
}
