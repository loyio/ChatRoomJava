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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.swing.*;

public class Client extends JFrame implements Runnable,ActionListener {
    Box baseBox;
    Button reg,login,send;
    TextField inputName,inputPw,inputIp,inputContent;
    JTextArea chatResult, atWho;
    JScrollPane chatResultjs;
    JList chatUsers;
    JScrollPane chatUsersJsp;
    String name="";
    Socket socket=null;
    DataInputStream in=null;
    DataOutputStream out=null;


    Client() {
        setLayout(new BorderLayout());
        Panel pNorth,pSouth;
        JPanel pRight;
        setTitle("聊天客户端");
        setLayout(new BorderLayout());

        pNorth=new Panel();
        pSouth=new Panel();
        pRight=new JPanel();
        chatUsers = new JList(new DefaultListModel());

        chatUsers.setSelectionModel(new DefaultListSelectionModel() {
            private static final long serialVersionUID = 1L;

            boolean gestureStarted = false;

            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(!gestureStarted){
                    if (index0==index1) {
                        if (isSelectedIndex(index0)) {
                            removeSelectionInterval(index0, index0);
                            return;
                        }
                    }
                    super.setSelectionInterval(index0, index1);
                }
                gestureStarted = true;
            }

            @Override
            public void addSelectionInterval(int index0, int index1) {
                if (index0==index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                    super.addSelectionInterval(index0, index1);
                }
            }

            @Override
            public void setValueIsAdjusting(boolean isAdjusting) {
                if (isAdjusting == false) {
                    gestureStarted = false;
                }
            }

        });
        chatUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (chatUsers.getSelectedValue() != null){
                    System.out.println(chatUsers.getSelectedValue().toString());
                    atWho.setText("@"+chatUsers.getSelectedValue().toString());
                }else{
                    atWho.setText("@所有人");
                }
            }
        });

        chatUsers.setListData(new String[]{" "});

        chatUsersJsp = new JScrollPane(chatUsers);
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
        pRight.setBorder(BorderFactory.createLineBorder(Color.black));
        pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
        pRight.add(new Label("在线用户"));
        pRight.add(Box.createVerticalStrut(5));
        pRight.add(chatUsersJsp);
        pNorth.add(new Label("用户名:"));
        pNorth.add(inputName);
        pNorth.add(new Label("密码:"));
        pNorth.add(inputPw);
        pNorth.add(new Label("服务器IP:"));
        pNorth.add(inputIp);
        pNorth.add(login);
        pNorth.add(reg);
        pSouth.add(new Label("输入聊天内容: "));
        atWho = new JTextArea();
        atWho.setText("@所有人");
        pSouth.add(atWho);
        pSouth.add(inputContent);
        pSouth.add(send);
        send.addActionListener(this);
        login.addActionListener(this);
        reg.addActionListener(this);
        inputName.addActionListener(this);
        inputContent.addActionListener(this);
        baseBox = Box.createHorizontalBox();
        add(pNorth,BorderLayout.NORTH);
        add(pSouth,BorderLayout.SOUTH);
        add(pRight, BorderLayout.EAST);
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
            try{
                if(socket==null){
                    socket=new Socket(InetAddress.getByName(inputIp.getText()).getHostAddress(), 4331);
                    in=new DataInputStream(socket.getInputStream());
                    out=new DataOutputStream(socket.getOutputStream());
                }
                out.writeUTF("name:"+name+"#"+inputPw.getText());
            } catch(IOException exp){}
        }else if(e.getSource()==reg){
            name=inputName.getText();

            try{
                if(socket==null){
                    socket=new Socket(InetAddress.getByName(inputIp.getText()).getHostAddress(), 4331);
                    in=new DataInputStream(socket.getInputStream());
                    out=new DataOutputStream(socket.getOutputStream());
                }
                out.writeUTF("register:"+name+"#"+inputPw.getText());
            }catch(IOException exp){}
        }else if(e.getSource()==send || e.getSource()==inputContent) {
            String s=inputContent.getText();
            String send_message = "chat%";
            if (chatUsers.getSelectedValue() != null){
                send_message += "dm@";
                send_message += chatUsers.getSelectedValue() + "#";
            }else {
                send_message += "bc#";
            }
            send_message = send_message+name +":" + s;
            System.out.println(send_message);
            if(s!=null) {
                try {
                    out.writeUTF(send_message);
                }catch(IOException e1){
                    System.out.println(e1);
                }
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
                    if (s.startsWith("user_online:")){
                        String user_list = s.substring(s.indexOf(":")+1);
                        Properties props = new Properties();
                        props.load(new StringReader(user_list.substring(1, user_list.length() - 1).replace(", ", "\n")));
                        Map<String, String> user_list_map = new HashMap<String, String>();
                        for (Map.Entry<Object, Object> e : props.entrySet()) {
                            user_list_map.put((String)e.getKey(), (String)e.getValue());
                        }
                        Iterator<Map.Entry<String,String>> user_iter = user_list_map.entrySet().iterator();
                        String[] userList = new String[10];
                        int i=0;
                        while (user_iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) user_iter.next();
                            userList[i++] = entry.getKey().toString();
                        }
                        chatUsers.setListData(userList);
                    }else {
                        System.out.println("receive 3");
                        chatResult.append("\n" + s);
                        System.out.println(s);
                    }
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
