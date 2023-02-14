import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class ClientFTP {
	private static DataOutputStream dataOutputStream = null;
	private static DataInputStream dataInputStream = null;

	public static void main(String[] args)
	{
		// Ask host and port to connect to from user
		
		/*System.out.println("Enter host and port of the server to connect");
		String host = sc.nextLine();
		int port = sc.nextInt();*/
		if(args.length<2)
			{
				System.out.println("Cannot connect to the server as localhost or post missing in the arguements");
				System.exit(0);
			}
		
		try (Socket socket = new Socket(args[0].toString(), Integer.parseInt(args[1]))) 
		{
			
			String usrInput = ""; 
			System.out.println("Connected to the server...\n");
			String currentUserDir = System.getProperty("user.dir");
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataOutputStream.writeUTF("pwd");
			String serverCurrentDir = dataInputStream.readUTF();
			Scanner sc = new Scanner(System.in);
			do
			{
				/*
				System.out.println("\nList of available commands\n");
				System.out.println("1. get - Fetch remote file from remote server ( get <remote_filename> )\n");
				System.out.println("2. put - Send local file to remote server ( put <local_filename> )\n");
				System.out.println("3. delete - Delete file from remote server ( delete <remote_filename> )\n");
				System.out.println("4. ls - List all files and subdirectories in the remote server ( ls )\n");
				System.out.println("5. cd - Change Directory to mentioned path or parent directory in the remote server ( cd <remote_directory_name> or cd .. )\n");
				System.out.println("6. mkdir - Create a subdirectory in current working directory in the remote server ( mkdir <remote_directory_name> )\n");
				System.out.println("7. pwd - Fetch present working directory in the remote server ( pwd )\n");
				System.out.println("8. quit - Close the connection with the remote server ( quit )\n\n");
				System.out.println("Enter your command to proceed : \n");*/
				System.out.print("mytftp>");
				usrInput = sc.nextLine();
				// String splitCommand[] = usrInput.split(" ");
				boolean bool = false;
				dataOutputStream.writeUTF(usrInput);
				switch(usrInput.split(" ")[0])
				{
					case  "put": 	System.out.println("Sending the File to the Server\n");
									//System.out.println(currentDir+"Client/Files/".concat(splitCommand[1]));
									sendFile(currentUserDir+"/".concat(usrInput.split(" ")[1]));
									break;

                    case  "get":    System.out.println("Fetching file from the Server");
									receiveFile(usrInput.split(" ")[1]);
									bool = dataInputStream.readBoolean();
									// System.out.println(bool);
									if(bool)
									{
										
										System.out.println("File Received Successfully");
									}
									else  
									{
										System.out.println("Failed to Receive File");
										
									}
					                break;

					case  "ls" :    String content = dataInputStream.readUTF();
					 				String content1[] = content.split(",");
									// System.out.println(content1);
									for (String child : content1){
										System.out.println(child);
									}
									break;

					case  "pwd" :  serverCurrentDir = dataInputStream.readUTF();
								   System.out.println(serverCurrentDir);
								   break;

					case "mkdir" : bool = dataInputStream.readBoolean();
								// System.out.println(bool);
					               if (bool == true){
									System.out.println("Directory created successfully");
								   }
								   else{
									System.out.println("Directory cannot be created");
								   }
								   break;

					case  "cd" :   bool = dataInputStream.readBoolean();
								   if (bool == true){
									
									
										System.out.println("Directory Changed");
										serverCurrentDir = dataInputStream.readUTF();
									
									
								   }
								   else{
									System.out.println("Not valid directory");
								   }
								   break;

					case "delete": 	bool = dataInputStream.readBoolean();
									System.out.println(bool);
									if (bool == true)
									{
										System.out.println("File deleted successfully");
					   				}
					   				else
									{
										System.out.println("File deletion Unsuccessful. File does not exist in the server");
					   				}
									break;

					case "quit":    dataInputStream.close();
									dataOutputStream.close();
									break;

					default : 		System.out.println("Please enter a valid command");
									break;
				}
			}while(!(usrInput.equals("quit")));	
			socket.close();	
		}
		catch(ConnectException ce)
		{
			System.out.println("Cannot connect to server. The host or port provided is incorrect. Please check and try again");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	//put : sending file to server, functon starts here
	private static void sendFile(String path) throws Exception
	{
		int bytes = 0;
		try{
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);

		// Here we send the File to Server
		dataOutputStream.writeLong(file.length());
		// Here we break file into chunks
		byte[] buffer = new byte[4 * 1024];
		while ((bytes = fileInputStream.read(buffer))
			!= -1) {
		// Send the file to Server Socket
		dataOutputStream.write(buffer, 0, bytes);
			dataOutputStream.flush();
		}
		// close the file here
		fileInputStream.close();
	}catch(FileNotFoundException e){
		System.out.println("File does not exist in the client");
			dataOutputStream.writeUTF("Fail");
			// return false;
	}
	}
    //get: getting file from server, function starts here

	private static void receiveFile(String fileName)
		throws Exception
	{
		if(fileName.contains("/"))
			{
				fileName = fileName.substring(fileName.lastIndexOf('/') + 1).trim();
			}
		int bytes = 0;
		// System.out.println("I am here");

		if (dataInputStream.readUTF().equals("Fail")){
			System.out.println("File does not exist at server");
			return;
		}
		FileOutputStream fileOutputStream = new FileOutputStream(fileName);
		// System.out.println("I am after fileoutput stream");

        
		long size = dataInputStream.readLong(); // read file size
		byte[] buffer = new byte[4 * 1024];
		while (size > 0
			&& (bytes = dataInputStream.read(
					buffer, 0,
					(int)Math.min(buffer.length, size)))
					!= -1) {
			// Here we write the file using write method
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes; // read upto file size
		}
		// Here we received file
		System.out.println("File is Received");
		fileOutputStream.close();
	}

	

}

