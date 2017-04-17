/*
 * MainFrame.java
 *
 * Created on 2016.8.19
 */

package com.yang.serialport.ui;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.yang.serialport.exception.NoSuchPort;
import com.yang.serialport.exception.NotASerialPort;
import com.yang.serialport.exception.PortInUse;
import com.yang.serialport.exception.SendDataToSerialPortFailure;
import com.yang.serialport.exception.SerialPortOutputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortParameterFailure;
import com.yang.serialport.exception.TooManyListeners;
import com.yang.serialport.manage.SerialPortManager;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.LoopQueue;
import com.yang.serialport.utils.ShowUtils;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 主界面
 * 
 * @author yangle
 */
public class MainFrame extends JFrame {

	/**
	 * 程序界面宽度
	 */
	public static final int WIDTH = 500;

	/**
	 * 程序界面高度
	 */
	public static final int HEIGHT = 360;

	private JTextArea dataView = new JTextArea();
	private JScrollPane scrollDataView = new JScrollPane(dataView);
	

	// 串口设置面板
	private JPanel serialPortPanel = new JPanel();
	private JLabel serialPortLabel = new JLabel("串口");
	private JLabel baudrateLabel = new JLabel("波特率");
	private JComboBox commChoice = new JComboBox();
	private JComboBox baudrateChoice = new JComboBox();

	// 操作面板
	private JPanel operatePanel = new JPanel();
	private JButton serialPortOperate = new JButton("打开串口");

	private List<String> commList = null;
	private SerialPort serialport;
	
	private PrintWriter printWriter;
	private JTextField tfFileName;

	public MainFrame() {
		initView();
		initComponents();
		actionListener();
		initData();

	}

	private void initView() {
		// 关闭程序
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, 505, 729);
		getContentPane().setLayout(null);

		setTitle("串口通讯");
	}

	private void initComponents() {
		// 数据显示
		dataView.setFocusable(false);
		scrollDataView.setBounds(10, 10, 475, 570);
		getContentPane().add(scrollDataView);

		// 串口设置
		serialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
		serialPortPanel.setBounds(10, 590, 170, 100);
		serialPortPanel.setLayout(null);
		getContentPane().add(serialPortPanel);

		serialPortLabel.setForeground(Color.gray);
		serialPortLabel.setBounds(10, 25, 40, 20);
		serialPortPanel.add(serialPortLabel);

		commChoice.setFocusable(false);
		commChoice.setBounds(60, 25, 100, 20);
		serialPortPanel.add(commChoice);

		baudrateLabel.setForeground(Color.gray);
		baudrateLabel.setBounds(10, 60, 40, 20);
		serialPortPanel.add(baudrateLabel);

		baudrateChoice.setFocusable(false);
		baudrateChoice.setBounds(60, 60, 100, 20);
		serialPortPanel.add(baudrateChoice);

		// 操作
		operatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
		operatePanel.setBounds(200, 590, 285, 100);
		operatePanel.setLayout(null);
		getContentPane().add(operatePanel);

		serialPortOperate.setFocusable(false);
		serialPortOperate.setBounds(10, 49, 90, 41);
		operatePanel.add(serialPortOperate);
		
		JButton btnNewButton = new JButton("清空");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				dataView.setText("");
				
			}
		});
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setBounds(110, 48, 165, 43);
		operatePanel.add(btnNewButton);
		
		tfFileName = new JTextField();
		tfFileName.setBounds(110, 17, 165, 21);
		operatePanel.add(tfFileName);
		tfFileName.setColumns(10);
		
		JLabel label = new JLabel("请输入文件名");
		label.setBounds(10, 20, 90, 15);
		operatePanel.add(label);
	}

	@SuppressWarnings("unchecked")
	private void initData() {
		commList = SerialPortManager.findPort();
		// 检查是否有可用串口，有则加入选项中
		if (commList == null || commList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			for (String s : commList) {
				commChoice.addItem(s);
			}
		}

		baudrateChoice.addItem("115200");
		baudrateChoice.addItem("9600");
		baudrateChoice.addItem("19200");
		baudrateChoice.addItem("38400");
		baudrateChoice.addItem("57600");
		
	}

	private void actionListener() {
		serialPortOperate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("打开串口".equals(serialPortOperate.getText())
						&& serialport == null) {
   
			        /* Open the serial */
			        openSerialPort(e);
					
					
				} else {
					printWriter.close();
					closeSerialPort(e);
					serialport = null;
				}
			}
		});
	}
	
	/**
	 * 打开串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void openSerialPort(java.awt.event.ActionEvent evt) {
		// 获取串口名称
		String commName = (String) commChoice.getSelectedItem();
		
		// 获取文件名
		String fileName = tfFileName.getText();
		if (fileName.isEmpty()) {
			
			int res=JOptionPane.showConfirmDialog(null, "请输入文件名后继续！", "请输入文件名后继续", JOptionPane.OK_OPTION);
		    if(res==JOptionPane.OK_OPTION){ 
		    	
		    	//System.out.println("ok");
		        return;  
		    }else if(res==JOptionPane.NO_OPTION){
		    	//System.out.println("no");
		    	return;
		    }else if(res==JOptionPane.CLOSED_OPTION){
		    	//System.out.println("close");
		    	return;
		    }
		}
		
		/* -1- Creat the directory */
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

		File directory = new File(".");
		String path = null;
		String myDir = "\\"+commName.toLowerCase()+"_dir";
		
		try {
			path = directory.getCanonicalPath();
		} catch (IOException exception) {exception.printStackTrace();} 
		path +=myDir;
		
		File dir =new File(path);  
		
		if(!dir.exists() && !dir.isDirectory())    
		{     
		    dir.mkdir();
		}
		
		/* -2- Creat the file */

		//String file_name = path+"\\"+df.format(new Date())+".txt";
		String file_name = path+"\\"+fileName+".txt";
		File file = new File(file_name);
		if (!file.exists()) {
			 try {
					file.createNewFile();
					FileWriter fileWriter = new FileWriter(file);
					printWriter = new PrintWriter(fileWriter);
				} catch (IOException exception) {exception.printStackTrace();}
		}
		else {
			int res=JOptionPane.showConfirmDialog(null, "文件已存在，请更换文件名！", "文件已存在", JOptionPane.OK_OPTION);
		    if(res==JOptionPane.OK_OPTION){ 
		    	
		    	//System.out.println("ok");
		        return;  
		    }else if(res==JOptionPane.NO_OPTION){
		    	//System.out.println("no");
		    	return;
		    }else if(res==JOptionPane.CLOSED_OPTION){
		    	//System.out.println("close");
		    	return;
		    }
		}
		
		
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) baudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				serialport = SerialPortManager.openPort(commName, baudrate);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
					serialPortOperate.setText("关闭串口");
					
				}
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		try {
			SerialPortManager.addListener(serialport, new SerialListener());
		} catch (TooManyListeners e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void closeSerialPort(java.awt.event.ActionEvent evt) {
		SerialPortManager.closePort(serialport);
		//dataView.setText("串口已关闭" + "\r\n");
		serialPortOperate.setText("打开串口");
	}

	private class SerialListener implements SerialPortEventListener {
		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {

			case SerialPortEvent.BI: // 10 通讯中断
				ShowUtils.errorMessage("与串口设备通讯中断");
				break;

			case SerialPortEvent.OE: // 7 溢位（溢出）错误

			case SerialPortEvent.FE: // 9 帧错误

			case SerialPortEvent.PE: // 8 奇偶校验错误

			case SerialPortEvent.CD: // 6 载波检测

			case SerialPortEvent.CTS: // 3 清除待发送数据

			case SerialPortEvent.DSR: // 4 待发送数据准备好了

			case SerialPortEvent.RI: // 5 振铃指示

			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
				break;

			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				byte[] data = null;
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					} else {
						// 读取串口数据
						data = SerialPortManager.readFromPort(serialport);

						for (byte b : data) {
							int c = (int)b & 0xFF;

							dataView.append(ByteUtils.byteToHex(b) + " ");

							dataView.setCaretPosition(dataView.getDocument().getLength());
							printWriter.print(ByteUtils.byteToHex(b) + " ");
							if (c == 0xff) {
								dataView.append(new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS").format(new Date()));
								dataView.append("\r\n");
								dataView.setCaretPosition(dataView.getDocument().getLength());
								
								printWriter.print(new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS").format(new Date()));
								printWriter.println();
							}
						}
						
						dataView.append("\r\n");
						dataView.setCaretPosition(dataView.getDocument().getLength());
						printWriter.println();

					}
				} catch (Exception e) {
					ShowUtils.errorMessage(e.toString());
					// 发生读取错误时显示错误信息后退出系统
					System.exit(0);
				}
				break;
			}
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
}