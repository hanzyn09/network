package mafia_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class Chat_Server {
      private static final int PORT = 9001; // ��Ʈ��ȣ
      private static HashSet<String> names = new HashSet<String>(); // �̸��� �����ϴ� hashset -> �ߺ� ����
      private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>(); // Ŭ���̾�Ʈ �ּҸ� �����ϴ� hashset -> �ߺ� ����
      private static HashMap<String, PrintWriter> info = new HashMap<String, PrintWriter>(); // �̸��� Ŭ���̾�Ʈ �ּҸ� �������ִ� hashmap
      private static int max_client = 7; // �ִ� Ŭ���̾�Ʈ ��
      private static int max_object = 10; // ������Ʈ�� ��(�ܼ�)
      private static int client_count = 0; // ���� ���� �������� ��

      private static int timer_flag = 0; // timeout�� �� Ŭ���̾�Ʈ���� ��
      private static int clickedNum = 0; // �� �Ͽ� ������Ʈ�� ���� ���ִ� �������� ��
      private static int current_client = max_client; // ���� �������� �� // client_count�� �ٸ� �� : client_count�� �������� ���� ��
                                          // count�Ǿ max_client�� ������ ������ ���۵ȴ�. ������ current client�� kick�� �������� �������� ���� ��Ÿ����
      
      private static int is_vote = 0; // ��ǥ�� �� �������� �� current_client �� �������� ó����
      private static int mafia_index = 0; // ���Ǿ��� �ε���
      private static int police_index = 0; // ������ �ε���
      private static int doctor_index = 0; // �ǻ��� �ε���
      private static int victim_index = 0; // ���Ǿư� ���̷��� �ϴ� ������� �ε���
      private static int[] temp = new int[] { -1, -1, -1 }; // room1�� room2�� user�� �̸����� ������, �ε����� ��ġ�� �ʰ� ���ִ� �迭

      private static boolean[] selectedByMafia = new boolean[max_client]; //���Ǿƿ��� ���õǾ����� �ȵǾ�ä������� ǥ���� false�� ���õ��� ������, true�� ���õȰ�
      private static boolean[] kicked = new boolean[max_client]; // ���� �Ǿ����� �ȵǾ����� ǥ�� false�� ����������� ��, true�� ����

      private static int[] vote = new int[max_client]; // �������� ��ǥ ���Ѽ�?

      private static String[] user = new String[max_client]; // ���� �̸� ���� -> �� ���� hashset�� �ִµ� �� ������? -> �ε����� �����ϱ� ����
      private static PrintWriter[] ID = new PrintWriter[max_client]; // ���� �ּ� ���� -> �� ���� hashset�� �ִµ� �� ������? -> �ε����� �����ϱ� ����
      private static String story = "Eight people went to the villa for vacation./"
            + "Then two of them fought and one person died./"
            + "You have to find the clue and find the culprit by voting."; // ���丮 ����
       private static String totalJob = ""; // ��� �������� �˷��� -> story�� ���� ����(��ư)
      private static String[] job = { "�ù� �ù��Դϴ�", "�ǻ� �ǻ��Դϴ�", "�ù� �ù��Դϴ�", "���Ǿ� ���Ǿ��Դϴ�", "�ù� �ù��Դϴ�", "���� �����Դϴ�",
            "�ù� �ù��Դϴ�" }; // ���� ���� �迭

      private static int[] random = { -1, -1, -1, -1, -1, -1, -1 }; // index�� ����ȭ �ϱ� ���ؼ� random ��̸� ������� -> �̶��� �ε����� ����

      private static int[] footSize = { 245, 250, 255, 260, 265, 270, 275 };
      private static int selectNum = 3; // ������Ʈ�� �����Ҽ��ִ� �������� ��
      private static int objectCount = 0; // ������Ʈ�� ������ �� (��ü)
      private static int[] canSelect = new int[selectNum]; // �������� ������Ʈ�� �����Ҽ��ִ��� ������ ǥ��
      private static boolean[] isClicked = new boolean[max_client]; // ������ ������Ʈ�� Ŭ���ߴ��� Ŭ������ �ʾҴ��� ǥ��
      private static boolean[] object_flag = new boolean[max_object]; // �ش� ������Ʈ�� ���õǾ����� �ȵǾ����� ����
      private static String[] object_msg = { user[police_index] + "�� ǰ���� ���� ������ ���Ҵ�.", 
            user[doctor_index] + "�� �񿡴� ���� �������� �ɷ��ִ�.", "���ڱ��� �� 1�� �̾��� �ֽ��ϴ�.", "���Ǿ� �濡 ���� ����", "���Ǿư� ���� �濡 ���� ����",
            "��ü �߻�����", "���Ǿ� �߻�����", "���� �߽����� ����� ���´ٰ� �Ѵ�", "��������Ʈ�� �ʹ� ����", "���� ������ ���־���." };


   /*
    * ������Ʈ�� ������ �� �ִ� ������� ���� �Լ�
    */
   private static void objectPerson() {
      int count = 0;
      while (true) {
         int client_value = (int) (Math.random() * max_client); // client_value�� 0~6���� ���� ���� ����
         int cnt = 0;

         /*
          * canSelect ��� ��ü ������ client_value�� ���� ���� �ִ��� ������ Ȯ����, ���� ���� ������ cnt������ ++ ��
          * cnt ������ 0�� �ƴ϶�� ���� �ߺ��Ǵ� ������ �ִٴ� ��
          */
         for (int i = 0; i < selectNum; i++) {
            if (client_value == canSelect[i])
               cnt++;
         }

         /*
          * cnt�� 0�̴� -> �ߺ��Ǵ� ���� ����. kicked[client_value]�� false�� ��� -> ������Ʈ�� ���� �����
          * index�� client_value������ �ϴµ�, false��� ���� �ش� ������ ��������� �ʾҴٴ� ���̴�.
          */
         if (cnt == 0 && kicked[client_value] == false) {
            canSelect[count] = client_value;
            count++;
         }

         /*
          * ���� 3���� ����� ������Ʈ�� �� ���ٸ�
          */
         if (count == selectNum)
            break;
      }
   }

   /*
    * �������� �ʱ�ȭ ��
    */
   private static void initialize() {
      for (int i = 0; i < max_client; i++) {
         kicked[i] = false;
         selectedByMafia[i] = false;
         vote[i] = 0;
         isClicked[i] = false;
         user[i] = "null";
         ID[i] = null;
      }

      for (int i = 0; i < selectNum; i++) {
         canSelect[i] = 9999;
      }

      for (int i = 0; i < max_object; i++)
         object_flag[i] = true;

      /*
       * story��ư�� ������ ������ ����
       */
      totalJob = "," + job[0].substring(0, job[0].indexOf(" ")) + "," + job[0].substring(job[0].indexOf(" ") + 1)
            + "," + job[1].substring(0, job[1].indexOf(" ")) + "," + job[1].substring(job[1].indexOf(" ") + 1) + ","
            + job[3].substring(0, job[3].indexOf(" ")) + "," + job[3].substring(job[3].indexOf(" ") + 1) + ","
            + job[5].substring(0, job[5].indexOf(" ")) + "," + job[5].substring(job[5].indexOf(" ") + 1);
   }

   /*
    * random ��̿� �������� �ߺ������ʰ� ����(0~6) ex : 1 5 3 6 2 4 0
    */
   private static void randomArray() {
      int index = 0;
      while (true) {
         int client_value = (int) (Math.random() * max_client);
         int cnt = 0;
         for (int i = 0; i < max_client; i++) {
            if (client_value == random[i])
               cnt++;
         }
         if (cnt == 0) {
            random[index] = client_value;
            index++;
         }
         if (index == max_client)
            break;
      }
   }

   /*
    * �ֿ� �������� �ε��� ����
    */
   public static void storeIndex() {
      for (int i = 0; i < max_client; i++) {
         if ((job[i].substring(0, job[i].indexOf(" "))).equals("���Ǿ�"))
            mafia_index = i;
         else if ((job[i].substring(0, job[i].indexOf(" "))).equals("����"))
            police_index = i;
         else if ((job[i].substring(0, job[i].indexOf(" "))).equals("�ǻ�"))
            doctor_index = i;
      }
   }

   /*
    * ������ �ǻ翡 ���� ������Ʈ �޼��� ����
    */
   private static void assignClue_pol_doc() {
      object_msg[0] = user[police_index] + "�� ǰ���� ���� ������ ���Ҵ�.";
      object_msg[1] = user[doctor_index] + "�� �񿡴� ���� �������� �ɷ��ִ�.";
   }

   /*
    * ���Ǿư� �ִ� �濡 �� �ִ� ����� ���
    */
   private static void assignClue_room1() {
      object_msg[3] = "room1," + user[mafia_index];
      int index = 0;

      while (true) {
         int client_value = (int) (Math.random() * max_client);
         int cnt = 0;
         if (client_value != mafia_index) {
            for (int i = 0; i < 3; i++) {
               if ((client_value == temp[i]))
                  cnt++;
            }
            if (cnt == 0) {
               temp[index] = client_value;
               object_msg[3] += "," + user[client_value];
               index++;
            }
            if (index == 3)
               break;
         }
      }
   }

   /*
    * ���Ǿư� ���� �濡 �� �ִ� ����� ���
    */
   private static void assignClue_room2() {
      object_msg[4] = "room2";
      int index = 0;
      int[] temp2 = new int[] { -1, -1, -1 };
      while (true) {
         int client_value = (int) (Math.random() * max_client);
         int cnt = 0;
         if (client_value != mafia_index) {
            for (int i = 0; i < 3; i++) {
               if ((client_value == temp[i] || client_value == temp2[i]))
                  cnt++;
            }
            if (cnt == 0) {
               temp2[index] = client_value;
               object_msg[4] += "," + user[client_value];
               index++;
            }
            if (index == 3)
               break;
         }
      }
   }

   /*
    * ��ü ������ �߻����� ũ�� ����
    */
   private static void assignClue_totalFootSize() {
      object_msg[5] = "foot size," + user[0] + " : " + footSize[0] + "," + user[1] + " : " + footSize[1] + ","
            + user[2] + " : " + footSize[2] + "," + user[3] + " : " + footSize[3] + "," + user[4] + " : "
            + footSize[4] + "," + user[5] + " : " + footSize[5] + "," + user[6] + " : " + footSize[6];
   }

   /*
    * ���Ǿ��� ��ũ��
    */
   private static void assignClue_mafiaFootSize() {
      object_msg[6] = "mafia foot size," + (footSize[mafia_index] - 5) + " ~ " + (footSize[mafia_index] + 5);
   }

   public static void main(String[] args) throws Exception {
      System.out.println("The chat server is running.");
      ServerSocket listener = new ServerSocket(PORT);

      randomArray();
      initialize();

      try {
         while (true) {
            new Handler(listener.accept()).start();
         }
      } finally {
         listener.close();
      }
   }

   private static class Handler extends Thread {

      private String name;
      private Socket socket;
      private BufferedReader in;
      private PrintWriter out;

      public Handler(Socket socket) {
         this.socket = socket;
      }

      public void run() {
         try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
               out.println("SUBMITNAME");
               name = in.readLine();
               if (name == null) {
                  return;
               }
               synchronized (names) {
                  sendToallclient("CONNECT " + name + " is connected.\n");

                  if (!names.contains(name)) {
                     names.add(name);

                     for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + "[" + name + "] enter");
                     }
                     break;
                  }
               }
            }
            out.println("NAMEACCEPTED");

            writers.add(out);
            user[client_count] = name;
            ID[client_count] = out;

            client_count++;

            System.out.println(user[client_count - 1] + "���� �����ϼ̽��ϴ�.");
            System.out.println("���� �ο� " + client_count + "��");

            info.put(name, out);
            for (int i = 0; i < selectNum; i++)
               System.out.println(canSelect[i]);
            if (client_count == max_client) {
               objectPerson();
               for (PrintWriter writer : writers) {
                  writer.println("MESSAGE " + "game start");
               }
               for (PrintWriter writer : writers) {
                  writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                        + user[canSelect[2]]);
               }

               for (int i = 0; i < max_client; i++) {
                  String temp = job[i];
                  job[i] = job[random[i]];
                  job[random[i]] = temp;
               }

               storeIndex();
               assignClue_pol_doc();
               assignClue_room1();
               assignClue_room2();
               assignClue_totalFootSize();
               assignClue_mafiaFootSize();
            }

            while (true) {
               if (kicked[mafia_index] == true) {
                  for (PrintWriter writer : writers) {
                     writer.println("MESSAGE " + "mafia dead!, citizen win!");
                  }
                  System.exit(0);
               } else if (current_client == 2) {
                  for (PrintWriter writer : writers) {
                     writer.println("MESSAGE " + "mafia win!");
                  }
                  System.exit(0);
               }
               String input = in.readLine();
               if (input == null) {
                  return;
               }

               /*
                * ������ job ��ư�� ������ client�κ��� /job ���������� �� ���(RoomGUI�� ���� ����)
                */
               else if (input.startsWith("/") && input.indexOf("job") != -1) {
                  int temp_index = 0;

                  for (int i = 0; i < client_count; i++) {
                     if (name.equals(user[i]))
                        temp_index = i;
                  }

                  PrintWriter sender = info.get(name);
                  sender.println("SHOW_JOB" + job[temp_index]);
               }

               /*
                * ������ story ��ư�� ������ client�κ��� /story ���������� �� ���(RoomGUI�� ���� ����)
                */
               else if (input.startsWith("/") && input.indexOf("story") != -1) {
                  int temp_index = 0;

                  for (int i = 0; i < client_count; i++) {
                     if (name.equals(user[i]))
                        temp_index = i;
                  }

                  PrintWriter sender = info.get(name);
                  sender.println("SHOW_STORY" + story + totalJob);
               }

               /*
                * �� ������ Ÿ�̸Ӱ� ���� ���
                */
               else if (input.startsWith("/") && input.indexOf("timeout") != -1) {
                  timer_flag++;
                  System.out.println(timer_flag);
                  /*
                   * ��� ������ Ÿ�̸Ӱ� ���� ���
                   */
                  if (timer_flag == current_client) {
                     String temp = null;
                     for (int i = 0; i < max_client; i++) {
                        if (kicked[i] == false) {
                           if (temp == null) {
                              temp = user[i];
                           } else {
                              temp += ("," + user[i]);
                           }
                        }
                     }
                     System.out.println(temp);
                     /*
                      * ��� �����鿡�� VOTENAME�̶�� ���������� ���� -> �������� ó���� ������� ����Ʈ�� ������
                      */
                     for (PrintWriter writer : writers) {
                        writer.println("VOTENAME " + temp);
                     }
                     timer_flag = 0; // ���� ��ǥ�� ���� 0���� �ʱ�ȭ
                  }
               }

               /*
                * �������� ��ǥ�� �ؼ� ���� ǥ�� �������� ����
                */
               else if (input.startsWith("/") && input.indexOf("victim") != -1) {
                  String victim = input.substring(7);
                  int temp_index = 0;
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(victim) && kicked[i] == false)
                        temp_index = i;
                  }
                  is_vote++;
                  vote[temp_index]++;
               }

               /*
                * ���� �Ǿ ������ ������ ��������
                */
               else if (input.startsWith("/") && input.indexOf("police") != -1) {
                  PrintWriter police = info.get(name);
                  String temp = null;

                  /*
                   * ������ ������ �������� ����� ����
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (kicked[i] == false && !user[i].equals(user[police_index])) {
                        if (temp == null) {
                           temp = user[i];
                        } else {
                           temp += ("," + user[i]);
                        }
                     }
                  }
                  System.out.println(temp);

                  /*
                   * ������ ���� �ʾҴٸ�
                   */
                  if (kicked[police_index] == false) {
                     /*
                      * /police���������� ���� user�� �̸��� ������ �̸��� ��ġ�Ѵٸ� �������� JOB�̶�� �������ݰ� �������� �̸��� ����
                      */
                     if (name.equals(user[police_index]))
                        police.println("JOB" + temp);
                  }
                  /*
                   * ������ �׾��ٸ�
                   */
                  else {
                     /*
                      * ���Ǿƿ� �̸��� ��ġ�ϴ� �������� NON�̶��� �������� ����
                      */
                     if (name.equals(user[mafia_index]))
                        police.println("NON");
                  }
               }
               
               /*
                 * ������ ������ �˰���� ����� ������ ���
                 */
               else if (input.startsWith("/") && input.indexOf("is_he_mafia?") != -1) {
                  PrintWriter police = info.get(user[police_index]);
                  String selected = input.substring(13);
                  int temp_index = 9999;
                  System.out.println("selected : " + selected);
                  
                  /*
                   *������ ������ ����� ��ġ�ϴ� ����� index ���� 
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        temp_index = i;
                     System.out.println("user[" + i + "] : " + user[i]);
                  }
                  
                  /*
                   * �������� IS_MAFIA?��� �������ݰ� ������ ������ ���� ���� ����
                   */
                  police.println("IS_MAFIA?" + user[temp_index] + "' job is "
                        + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
               }
               
               /*
                * ���Ǿƿ��Լ� /kill���������� �´ٸ�
                */
               else if (input.startsWith("/") && input.indexOf("kill") != -1) {
                  PrintWriter mafia = info.get(user[mafia_index]); // ���Ǿ��� �ּ� ����
                  String temp = null;

                  /*
                   * ���Ǿƿ� ���� ������� ������ ��� ����
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (kicked[i] == false && !user[i].equals(user[mafia_index])) {
                        if (temp == null) {
                           temp = user[i];
                        } else {
                           temp += ("," + user[i]);
                        }
                     }
                  }
                  System.out.println("mafia " + temp);

                  /*
                   * ���Ǿ� ���� KILL�̶�� �������ݰ� ��� ����
                   */
                  mafia.println("KILL" + temp);
                  System.out.println("���Ǿ� ��� �Ѿ");

               }
               /*
                * ���Ǿư� ���� ����� ���ߴٸ�
                */
               else if (input.startsWith("/") && input.indexOf("dead") != -1) {
                  String selected = input.substring(5); // ���� ���(victim)�� �̸� ����
                  PrintWriter dead = info.get(selected);
                  /*
                   * ���Ǿƿ��� ���ô��� ����� index ����
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (user[i].equals(selected) && kicked[i] == false)
                        victim_index = i;
                  }

                  /*
                   * ���� �ǻ簡 ����ִٸ�
                   */
                  if (kicked[doctor_index] == false) {
                     PrintWriter doctor = info.get(user[doctor_index]);
                     selectedByMafia[victim_index] = true;

                     String temp = null;
                     /*
                      * ������� ���� �ڽ��� ������ ��� ����
                      */
                     for (int i = 0; i < max_client; i++) {
                        if (kicked[i] == false) {
                           if (temp == null) {
                              temp = user[i];
                           } else {
                              temp += ("," + user[i]);
                           }
                        }
                     }
                     /*
                      * �ǻ翡�� DOCTOR �������ݰ� ��� ����
                      */
                     doctor.println("DOCTOR" + temp);
                  }

                  /*
                   * ���� �ǻ簡 �׾��ٸ�
                   */
                  else {
                     /*
                      * ���� ���� ������� KICKED��� �������� ���� -> ����
                      */
                     dead.println("KICKED");

                     /*
                      * ��ο��� victim�� ������ ������ broadcasting�ϸ鼭 D_START -> ���� �Ǿ��ٰ� �˸�
                      */
                     for (PrintWriter writer : writers) {
                        writer.println("D_START" + user[victim_index] + " dead, he was "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")));
                     }
                     kicked[victim_index] = true; // ������� ������ ���ӿ��� ����
                     current_client--; // �Ѹ� ���� ����
                     objectPerson(); // ������Ʈ�� �� ����� �ٽ� �������� ��

                     /*
                      * ���� ��� ������Ʈ�� Ŭ���ߴٸ�
                      */
                     if (objectCount == max_object) {
                        /*
                         * ��� �������� T_START(Ÿ�̸ӽ���) ���������� ����
                         */
                        for (PrintWriter writer : writers) {
                           writer.println("T_START" + "all object selected");
                        }
                     }
                     /*
                      * ���� ������ object�� ���Ҵٸ�
                      */
                     else {
                        /*
                         * ��� �����鿡�� �ܼ��� ���� ������� ����� �˷���(CLUEFINEDER ��������)
                         */
                        for (PrintWriter writer : writers) {
                           writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                                 + user[canSelect[2]]);
                        }

                     }

                  }
               }

               /*
                * �ǻ簡 �츱 ����� �����ߴٸ�
                */
               else if (input.startsWith("/") && input.indexOf("protect") != -1) {
                  PrintWriter dead = info.get(user[victim_index]);
                  int temp_index = 9999;
                  String protect = input.substring(8);

                  /*
                   * �ǻ簡 �츰 ����� index ����
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (protect.equals(user[i]))
                        temp_index = i;
                  }

                  selectedByMafia[temp_index] = false;// ���Ǿƿ��� ���ô��� ���� ��ҽ�Ŵ -> ���� false�� ����� false�� ����������� -> �����
                                             // ���츮�� ���

                  /*
                   * �ǻ簡 ���Ǿƿ��� ���ô��� ����� �������� ���� ���
                   */
                  if (selectedByMafia[victim_index] == true) {
                     /*
                      * ����� ��Ƽ
                      */
                     dead.println("KICKED");

                     /*
                      * �������� /dead �������ݰ� ���
                      */
                     for (PrintWriter writer : writers) {
                        writer.println("D_START" + user[victim_index] + " dead, he was "
                              + job[victim_index].substring(0, job[victim_index].indexOf(" ")));
                     }
                     kicked[victim_index] = true;
                     current_client--;
                     objectPerson();
                     if (objectCount == max_object) {
                        for (PrintWriter writer : writers) {
                           writer.println("T_START" + "all object selected");
                        }

                     } else {
                        for (PrintWriter writer : writers) {
                           writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                                 + user[canSelect[2]]);
                        }
                     }

                  }

                  /*
                   * �ǻ簡 ���Ǿƿ��� ���ô��� ����� ������ ���
                   */
                  else {
                     /*
                      * ��� �������� �ǻ簡 ����ڸ� ��ȴٰ� ��ε�ĳ��Ʈ
                      */
                     for (PrintWriter writer : writers) {
                        writer.println("D_START" + "Doctor saved victim");
                     }

                     objectPerson(); // ������Ʈ�� �� ��� ���� ����

                     if (objectCount == max_object) {
                        for (PrintWriter writer : writers) {
                           writer.println("T_START" + "all object selected");
                        }
                     } else {
                        for (PrintWriter writer : writers) {
                           writer.println("CLUEFINDER" + user[canSelect[0]] + "," + user[canSelect[1]] + ","
                                 + user[canSelect[2]]);
                        }
                     }
                  }
               }

               /*
                * ������ ������Ʈ�� ���� ���
                */
               else if (input.startsWith("object_clicked")) {
                  int msg_index = Integer.parseInt(input.substring(14));
                  
                  /*
                   * ������Ʈ�� ���� ������ �̸��� ������Ʈ�� �������� ���� ������ �̸��� ���ٸ�.
                   */
                  if (name.equals(user[canSelect[clickedNum]])) {
                     
                     /*
                      * ������Ʈ�� ���õ��� �ʾҰ� (object_flag[msg_index] == true ) �ش� ������ �ٸ� ������Ʈ�� Ŭ�� ���� �ʾҴٸ�(isClicked[canSelect[clickedNum]] == false)
                      */
                     if (object_flag[msg_index] == true && isClicked[canSelect[clickedNum]] == false) {
                        PrintWriter sendObject = info.get(name);

                        System.out.println(msg_index);
                        
                        /*
                         * ù��° �ι�° ������ ������Ʈ�� �����ϴ� ���
                         */
                        if (clickedNum != selectNum - 1) {
                           
                            /*
                             * ��� �������� �ش� ������ ������Ʈ�� �̾����� �˷��ְ� ���� ������ ������Ʈ�� ���� ���ʶ�� ���� �˷���
                             */
                           for (PrintWriter writer : writers) {
                              
                              /*
                               * ���� ������ ������Ʈ�� ���� �ִ� ���
                               */
                              if (objectCount != max_object - 1) {
                                 writer.println("FOUND" + user[canSelect[clickedNum]] + ","
                                       + user[canSelect[clickedNum + 1]]);
                              } 
                              /*
                               *�ش� ������ ������Ʈ�� �̾Ƽ� ���̻� ������ ������Ʈ�� ���� ���(10�� ��� ������ ���) 
                               */
                              else {
                                 writer.println(
                                       "FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                              }

                           }
                           sendObject.println("object_description" + object_msg[msg_index]); // ����ڰ� ���� ������Ʈ�� �ش��ϴ� �޼��� ����
                           object_flag[msg_index] = false; // �ش� �������z ��Ȱ��ȭ
                           clickedNum++; // Ŭ���� Ƚ�� ����

                        } 
                        
                        /*
                         *������ ������ ������Ʈ�� �����ϴ� ��� 
                         */
                        else {
                           for (PrintWriter writer : writers) {
                              writer.println("FOUND" + user[canSelect[clickedNum]] + "," + "everyone_select");
                           }
                           sendObject.println("object_description" + object_msg[msg_index]);
                           object_flag[msg_index] = false;
                           clickedNum++;
                        }
                        objectCount++;
                        
                        /*
                         * �ش� ������ ������Ʈ�� �����ؼ� ���̻� ������ ������Ʈ�� ���� ���(10�� ��� ����)
                         */
                        if (objectCount == max_object) {
                           for (PrintWriter writer : writers) {
                              writer.println("T_START" + "all object selected");
                           }
                        }
                     } 
                     
                     /*
                      * �̹� �ٸ� ����� �ش� ������Ʈ�� ������ ��� -> �ش� ������ �ٸ� ������Ʈ�� ���� �� ����
                      */
                     else if (object_flag[msg_index] == false && isClicked[canSelect[clickedNum]] == false) {
                        PrintWriter sendObject = info.get(name);
                        sendObject.println("object_description" + "�̹� �ٸ� ����� ������ ������Ʈ�Դϴ�.");
                     }
                  } 
                  
                  /*
                   * �ش� ������ ���ʰ� �ƴ� ���
                   */
                  else {
                     PrintWriter sendObject = info.get(name);
                     sendObject.println("object_description" + "����� ���ʰ� �ƴմϴ�");
                  }

                  /*
                   * ������Ʈ�� �̵��� ������ ����(3��)�� �� ���� ���
                   */
                  if (clickedNum == selectNum) {
                     
                     /*
                      * ��� �����鿡�� Ÿ�̸� ���� �������� ����
                      */
                     for (PrintWriter writer : writers) {
                        writer.println("T_START");
                     }
                     clickedNum = 0;
                     
                     /*
                      * �ٽ� �ʱ�ȭ -> ���� �Ͽ� ������Ʈ�� ���� �������� �ε����� �����ؾ� �ϱ� ����
                      */
                     for (int i = 0; i < selectNum; i++) {
                        canSelect[i] = 9999;
                     }
                  }

               } 
               
               /*
                * �׳� ä�� ġ�� ���
                */
               else {
                  for (PrintWriter writer : writers) {
                     if (!input.equals("")) // ����Ű�� ��� ������ �޼��� ���� ���鹮�ڸ� ��µǴ� ��츦 �����ϰ�
                        writer.println("MESSAGE " + name + ": " + input);
                  }
               }

               
               /*
                * ��� �������� ��ǥ�� ���ƴٸ�
                */
               if (is_vote == client_count) {
                  int count = 0;
                  int temp_index = 0;
                  int same = 0;
                  
                  /*
                   * ���� ���� ǥ�� ���� ������ ã�Ƴ�
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (vote[i] > count) {
                        count = vote[i];
                        temp_index = i;
                     }
                  }
                  
                  /*
                   * �ѹ� �� �˻��ؼ� ������  �ִ��� ã�Ƴ�
                   */
                  for (int i = 0; i < max_client; i++) {
                     if (count == vote[i] && i != temp_index)
                        same = 1;
                  }

                  if (count == 0)
                     same = 0;

                  /*
                   * ������ �ƴ� ���
                   */
                  if (same != 1) {
                     PrintWriter victim = info.get(user[temp_index]);
                     victim.println("KICKED");
                     for (PrintWriter writer : writers) {
                        writer.println("V_END" + user[temp_index] + " dead, he was "
                              + job[temp_index].substring(0, job[temp_index].indexOf(" ")));
                     }
                     kicked[temp_index] = true;
                     current_client--;
                  } 
                  
                  /*
                   * ������ ���
                   */
                  else {
                     for (PrintWriter writer : writers) {
                        writer.println("V_END" + "Nothing happened");
                     }
                  }
                  is_vote = 0;
                  count = 0;
                  
                  /*
                   * ���� ��ǥ�� ���� �ʱ�ȭ
                   */
                  for (int i = 0; i < max_client; i++)
                     vote[i] = 0;
               }
            }
         } catch (IOException e) {
            System.out.println(e);
         } finally {
            if (name != null) {
               names.remove(name);
               info.remove(name);
               client_count--;
               System.out.println("�Ѹ� ������ " + client_count);
            }
            if (out != null) {
               writers.remove(out);
            }
            try {
               socket.close();
            } catch (IOException e) {
            }
         }
      }

   }

   public static void sendToallclient(String mssg) {
      for (PrintWriter writer : writers) {
         writer.println(mssg);
         writer.flush();
      }
   }
}