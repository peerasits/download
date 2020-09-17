
import java.io.InputStream;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;

public class MultiThreadDownloadGUI extends JFrame {
	private String urlAddress = "http://212.183.159.230/1GB.zip";
	private MultiThreadDownloadGUI obj;
	private DownloadThread[] threads;
	private int fileSize;
	private int threadCount;
	private int blockSize;

	// gui
	private JPanel panel;
	private JProgressBar[] progresses;
	private JProgressBar overall;
	private JTextField[] textes;
	private JTextField urlText;
	private JButton submitBtn;
	private JTextPane status;

	public MultiThreadDownloadGUI() {
		panel = new JPanel();
		panel.setBackground(new Color(153,255,255));
		urlText = new JTextField();
		urlText.setColumns(60);
		panel.add(urlText);
		submitBtn = new JButton("Download");
		submitBtn.addActionListener(new ButtonActionListener());
		panel.add(submitBtn);
		status = new JTextPane();
		status.setPreferredSize(new Dimension(700, 300));
		panel.add(status);

		add(panel);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	class ButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == submitBtn) {
				obj.process();
			}
		}
	}

	public void initProgress() {

		
		textes = new JTextField[threadCount];
		progresses = new JProgressBar[threadCount];
		for (int i = 0; i < threadCount; i++) {
			textes[i] = new JTextField();
			textes[i].setText("Thread : " + (i + 1));
			progresses[i] = new JProgressBar();
			progresses[i].setStringPainted(true);
			panel.add(textes[i]);
			panel.add(progresses[i]);
		}
		
		overall = new JProgressBar();
		overall.setStringPainted(true);
		panel.add(overall);
		add(panel);
		setVisible(true);
	}


	public void process() {
		String printStatus = "";
		String urlAddress = urlText.getText().toString();
		printStatus = printStatus.concat("You entered " + urlAddress + "\n" + "connecting to server...");
		status.setText(printStatus);

		try {
			URL url = new URL(urlAddress);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			String fileName = url.getFile();
			int index = fileName.lastIndexOf("/") + 1;
			fileName = fileName.substring(index);
			printStatus = printStatus.concat("\n" + fileName + "\n" + "total file size : " + conn.getContentLength()+"\n");
			status.setText(printStatus);

			fileSize = conn.getContentLength();

			RandomAccessFile file = new RandomAccessFile("E:\\" + fileName, "rw");
			file.setLength(fileSize);
			file.close();

			threadCount = 10;
			blockSize = fileSize / threadCount == 0 ? fileSize / threadCount : fileSize / threadCount + 1;

			printStatus = printStatus.concat("Each thread should download: " + blockSize + "\n"
					+ "Each thread should download in mb: " + blockSize / 1024 / 1024 + "MB"+"\n");
			status.setText(printStatus);
			threads = new DownloadThread[threadCount];

			// gui
			initProgress();
			

			for (int i = 0; i < threadCount; i++) {
				int start = i * blockSize;
				int end = start + (blockSize - 1);
				printStatus = printStatus.concat("Thread " + (i + 1) + " : = " + start + "," + end+"\n");
				status.setText(printStatus);
				if(i==0) {
					DownloadThread.overAll = overall;
				}
				threads[i] = new DownloadThread(fileName, start, end, url, progresses[i]);
				threads[i].start();
				
			}


		} catch (Exception e) {
			printStatus = printStatus.concat(e.getMessage());
			status.setText(printStatus);
		}
	}

	public static void main(String[] args) {
		MultiThreadDownloadGUI multiDownload = new MultiThreadDownloadGUI();
		multiDownload.obj = multiDownload;

	}

	static class DownloadThread extends Thread {
		private String fileName;
		private int start;
		private int end;
		private URL url;
		static int sum = 0;
		static JProgressBar overAll;

		JProgressBar nowProgress;

		public DownloadThread(String fileName, int start, int end, URL url, JProgressBar p) {
			this.fileName = fileName;
			this.start = start;
			this.end = end;
			this.url = url;
			nowProgress = p;
		}

		public int progress(int start, int end, int now, String name) {
			int beforeCount = -1;
			int size = end - start;

			int blocks = size / 100;
			String str = name.concat(" progress : ");
			int count = 0;
			for (int i = 1; i <= 100; i++) {

				if (now == end) {
					str = str.concat("#");
				} else if (now > i * blocks && now != end) {
					str = str.concat("#");
					count++;
				} else
					str = str.concat("-");
				nowProgress.setValue(count);
			}

			return count;

		}

		public void run() {
			try {
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("Range", "bytes=" + start + "-" + end);

				if (con.getResponseCode() == 206) {
					InputStream in = con.getInputStream();
					RandomAccessFile out = new RandomAccessFile("E:\\" + fileName, "rwd");
					out.seek(start);
					byte[] b = new byte[1024];
					int len = 0;
					int i = 1;
					int preV = 0,nowCount = 0;
					while ((len = in.read(b)) != -1) {
						out.write(b, 0, len);
						nowCount =  progress(start, end, 1024 * i, this.getName());
						if(nowCount>preV) {
							sum++;
							preV = nowCount;
						}
						overAll.setValue(sum/10);
						i++;
					}
					out.close();
					in.close();
					System.err.println();
					System.err.println(this.getName() + " completes ");
					Thread.sleep(10);

				}

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}