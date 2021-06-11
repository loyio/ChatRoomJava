package me.loyio.chatroom;

/**
 * Project: ChatRoom
 * Package: me.loyio.chatroom
 * User: loyio
 * Date: 6/11/21
 */
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
public class Client extends JFrame implements Runnable,ActionListener {
    Button reg,login,send;
    TextField inputName,inputPw,inputIp,inputContent;
    JTextArea chatResult;
    JScrollPane chatResultjs;
    String name="";
    Socket socket=null;
    DataInputStream in=null;
    DataOutputStream out=null;


    Client() {
        setLayout(new BorderLayout());
        Panel pNorth,pSouth;
        setTitle("聊天客户端");
        setLayout(new BorderLayout());
        pNorth=new Panel();
        pSouth=new Panel();
        inputName=new TextField(6);
        inputPw=new TextField(6);
        inputIp=new TextField(12);
        inputContent=new TextField(22);
        send=new Button("发送");
        reg=new Button("注册");
        login=new Button("登录");
        send.setEnabled(false);
        chatResult =new JTextArea();
        chatResult.setFont(new Font("宋体",Font.BOLD,20));
        chatResultjs=new JScrollPane(chatResult);
        pNorth.add(new Label("用户名:"));
        pNorth.add(inputName);
        pNorth.add(new Label("密码:"));
        pNorth.add(inputPw);
        pNorth.add(new Label("服务器IP:"));
        pNorth.add(inputIp);
        pNorth.add(login);
        pNorth.add(reg);
        pSouth.add(new Label("输入聊天内容:"));
        pSouth.add(inputContent);
        pSouth.add(send);
        send.addActionListener(this);
        login.addActionListener(this);
        reg.addActionListener(this);
        inputName.addActionListener(this);
        inputContent.addActionListener(this);
        add(pNorth,BorderLayout.NORTH);
        add(pSouth,BorderLayout.SOUTH);
        add(chatResultjs,BorderLayout.CENTER);
        setBounds(100, 100, 550,300);
        setVisible(true);
        validate();
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    public void actionPerformed(ActionEvent e){
        if(e.getSource()==login){
            name=inputName.getText();
            send.setEnabled(true);
            this.setTitle(name);
            try{if(socket==null){
                socket=new Socket(InetAddress.getByName(inputIp.getText()).getHostAddress(), 4331);
                in=new DataInputStream(socket.getInputStream());
                out=new DataOutputStream(socket.getOutputStream());
            }
                out.writeUTF("姓名:"+name+"#"+inputPw.getText());
            }
            catch(IOException exp){}
        }else  if(e.getSource()==reg){
            name=inputName.getText();

            try{if(socket==null){
                socket=new Socket(InetAddress.getByName(inputIp.getText()).getHostAddress(), 4331);
                in=new DataInputStream(socket.getInputStream());
                out=new DataOutputStream(socket.getOutputStream());
            }
                out.writeUTF("注册:"+name+"#"+inputPw.getText());
            }
            catch(IOException exp){}
        }else if(e.getSource()==send || e.getSource()==inputContent)
        {  String s=inputContent.getText();
            if(s!=null)
            {  try { out.writeUTF("聊天内容:"+name+":"+s);
            }
            catch(IOException e1){System.out.println(e1);}
            }
        }
    }
    public void run(){
        String s=null;
        while(true){
            try{
                Thread.sleep(100);
                if(in!=null){
                    s=in.readUTF();
                    System.out.println("receive 3");
                    chatResult.append("\n"+s);
                    System.out.println(s);
                }
            }
            catch(IOException e){
                chatResult.setText("和服务器的连接关闭");
                break;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    public static void main(String args[]){
        Client c=new Client();
        new Thread(c).start();

    }
}
