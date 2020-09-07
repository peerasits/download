

import java.util.Scanner;


import java.io.InputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class MultiThreadDownload {
	private static String urlAddress = "http://212.183.159.230/1GB.zip";
	private static int fileSize;
	private static int threadCount;
	private static int blockSize;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter URL to download : ");
		urlAddress = sc.nextLine();
		
		System.out.println("You entered "+ urlAddress);
		System.out.println("connecting to server...");
		
		try {
			Thread.sleep(1000);
			URL url = new URL(urlAddress);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			
			String fileName = url.getFile();
			int index = fileName.lastIndexOf("/")+1;
			fileName = fileName.substring(index);
			System.out.println(fileName);

			System.out.println("total file size : " + conn.getContentLength());
			fileSize = conn.getContentLength();


			RandomAccessFile file = new RandomAccessFile("E:\\"+fileName, "rw");
			file.setLength(fileSize);
			file.close();

			threadCount = 10;
			blockSize = fileSize / threadCount == 0 ? fileSize / threadCount : fileSize / threadCount + 1;
			System.out.println("Each thread should download: " + blockSize);
			System.out.println("Each thread should download in mb: " + blockSize / 1024 / 1024 + "MB");

			for (int i = 0; i < threadCount; i++) {
				int start = i * blockSize;
				int end = start + (blockSize - 1);
				System.out.println("Thread "+i+" : = " + start + "," + end);
				new DownloadThread(fileName, start, end,url).start();
			}
			
			byte[] b=new byte[1024];

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	static class DownloadThread extends Thread {
		private String fileName;
		private int start;
		private int end;
		private URL url;

		public DownloadThread(String fileName, int start, int end, URL url) {
			this.fileName = fileName;
			this.start = start;
			this.end = end;
			this.url = url;
		}
		
		public static boolean progress(int start, int end, int now,String name,boolean print) {
			int size = end - start;

			int blocks = size / 100;
			String str = name.concat(" progress : ");
			int count = 0;
			for (int i = 1; i <= 100; i++) {
				
				if(now == end) {
					str = str.concat("#");
				}
				else if (now > i * blocks && now != end) {
					str = str.concat("#");
					count++;
				} else
					str = str.concat("-");

			}
			
			if(count == 100) {
				if(!print)
					System.out.println(str+" ("+count+" %)");
				return true;
			}
			else {
				System.out.println(str+" ("+count+" %)");
				return false;
			}
		}

		public void run() {
			try {
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Range","bytes="+start+"-"+end);
				
				if(con.getResponseCode() == 206) {
					InputStream in = con.getInputStream();
					RandomAccessFile out = new RandomAccessFile("E:\\"+fileName,"rwd");
					out.seek(start);
					byte[] b=new byte[1024];
	                  int len = 0;
	                  int i = 1;
	                  boolean value = false;
	                  while((len=in.read(b))!=-1){
	                     out.write(b,0,len);
	                     value = progress(start,end,1024*i,this.getName(),value);
	                     i++;
	                  }
	                  out.close();
	                  in.close();
	                  System.err.println();
	                  System.err.println(this.getName()+" completes ");
	                  Thread.sleep(10);
	                  
				}
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
