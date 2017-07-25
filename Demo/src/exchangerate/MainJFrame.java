package exchangerate;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Desktop;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Font;

public class MainJFrame {

	private JFrame frame;
	private JPanel resultPanel;
	private JTextField srcNum;
	private JLabel srcCur;
	private JLabel equals;
	private JTextField dstNum;
	private JLabel dstCur;
	private JPanel inputPanel;
	private JTextField input;
	private JTextField resultTxt;
	private JPanel commonConvert;

	private static Vector<String> sampleCorpus = new Vector<String>();
	private JButton button_1;
	static {
		sampleCorpus.add("帮我查一下汇率");
		sampleCorpus.add("今日汇率");
		sampleCorpus.add("今天的汇率有什么变化");
		
		sampleCorpus.add("美元的汇率是多少");
		sampleCorpus.add("查一下新加坡的汇率");
		sampleCorpus.add("我要去韩国玩，帮我看一下汇率信息");
		sampleCorpus.add("帮我查询一下中国与各国货币的汇率表");
		
		sampleCorpus.add("美元兑换成人民币");
		
		sampleCorpus.add("欧元是什么国家的货币");
		sampleCorpus.add("中国用什么货币");
		sampleCorpus.add("欧元在哪些国家使用");
		sampleCorpus.add("新加坡的货币是什么单位");
		
		sampleCorpus.add("你会不会查汇率啊");
		sampleCorpus.add("你能告诉我澳元的汇率吗");
		sampleCorpus.add("你知道港元跟日元之间的汇率吗");
		sampleCorpus.add("你能帮我计算什么币种的汇率");
		
		sampleCorpus.add("一百美元能换多少日元");
		sampleCorpus.add("10美元能换到60元人民币吗");
		sampleCorpus.add("多少美元可以兑换1000人民币");
		sampleCorpus.add("我要换一百美元需要多少人民币");
		
		//听不懂
		//sampleCorpus.add("调皮捣蛋听不懂");
	}
	public static String getRandomSampleCorpus() {
		int randomInt = (int)(Math.random() * sampleCorpus.size());
		Utils.p("random:" + randomInt + "/" + sampleCorpus.size());
		return sampleCorpus.get(randomInt);
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainJFrame window = new MainJFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainJFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("自然语言理解试验小程序——汇率换算");
		frame.setBounds(100, 100, 915, 440);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel semanticPanelBottom = new JPanel();
		semanticPanelBottom.setBounds(20, 77, 412, 293);
		semanticPanelBottom.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "  \u8BED \u4E49  ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		semanticPanelBottom.setToolTipText(" 123");
		frame.getContentPane().add(semanticPanelBottom);
		semanticPanelBottom.setLayout(null);
		
		JScrollPane semanticPanel = new JScrollPane();
		semanticPanel.setBounds(10, 26, 392, 250);
		semanticPanelBottom.add(semanticPanel);
		semanticPanel.setToolTipText("语义");
		
		final JTextArea semanticTxt = new JTextArea();
		semanticPanel.setViewportView(semanticTxt);
		semanticTxt.setColumns(20);
		semanticTxt.setRows(5);
		semanticTxt.setEditable(false);
		
		resultPanel = new JPanel();
		resultPanel.setBounds(442, 77, 428, 148);
		resultPanel.setBorder(new TitledBorder(null, "  \u7ED3 \u679C  ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		frame.getContentPane().add(resultPanel);
		resultPanel.setLayout(null);
		
		resultTxt = new JTextField();
		resultTxt.setFont(new Font("宋体", Font.PLAIN, 18));
		resultTxt.setBounds(30, 32, 374, 42);
		resultPanel.add(resultTxt);
		resultTxt.setColumns(10);
		
		commonConvert = new JPanel();
		commonConvert.setBounds(30, 96, 374, 42);
		resultPanel.add(commonConvert);
		commonConvert.setLayout(null);
		
		srcNum = new JTextField();
		srcNum.setBounds(10, 10, 66, 21);
		commonConvert.add(srcNum);
		srcNum.setHorizontalAlignment(SwingConstants.CENTER);
		srcNum.setEditable(false);
		srcNum.setColumns(10);
		srcNum.setBackground(Color.WHITE);
		
		srcCur = new JLabel("s");
		srcCur.setBounds(85, 10, 50, 21);
		commonConvert.add(srcCur);
		
		equals = new JLabel("=");
		equals.setBounds(135, 13, 23, 15);
		commonConvert.add(equals);
		equals.setHorizontalAlignment(SwingConstants.CENTER);
		
		dstNum = new JTextField();
		dstNum.setHorizontalAlignment(SwingConstants.CENTER);
		dstNum.setBounds(168, 10, 66, 21);
		commonConvert.add(dstNum);
		dstNum.setBackground(Color.WHITE);
		dstNum.setEditable(false);
		dstNum.setColumns(10);
		
		dstCur = new JLabel("d");
		dstCur.setBounds(240, 13, 49, 15);
		commonConvert.add(dstCur);
		
		inputPanel = new JPanel();
		inputPanel.setBounds(20, 10, 850, 57);
		frame.getContentPane().add(inputPanel);
		inputPanel.setLayout(null);
		
		JButton submit = new JButton("理解");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputStr = input.getText();
				ExHandler handler = new ExHandler(inputStr);
				semanticTxt.setText(handler.getNliResultForShow());
				resultTxt.setText(handler.getResultString());
				setCommonConvert(handler);
			}
		});
		submit.setBounds(109, 9, 70, 40);
		inputPanel.add(submit);
		
		input = new JTextField();
		input.setFont(new Font("宋体", Font.PLAIN, 18));
		input.setText(getRandomSampleCorpus());
		input.setColumns(10);
		input.setBounds(204, 8, 622, 39);
		inputPanel.add(input);
		
		JButton button = new JButton("换一句");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				input.setText(getRandomSampleCorpus());
			}
		});
		button.setBounds(20, 9, 79, 40);
		inputPanel.add(button);
		
		button_1 = new JButton("源码和功能说明");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("http://blog.csdn.net/happycxz/article/details/73223916"));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		button_1.setFont(new Font("宋体", Font.PLAIN, 20));
		button_1.setBounds(570, 271, 185, 57);
		frame.getContentPane().add(button_1);
	}
	
	private void setCommonConvert(ExHandler handler) {
		commonConvert.setVisible(handler.detailContext.isVisible);
		
		if (handler.detailContext.isVisible) {
			srcNum.setText(handler.detailContext.srcN);
			dstNum.setText(handler.detailContext.dstN);
			srcCur.setText("<html><font color=red>" + handler.detailContext.srcC + "</font></html>");
			dstCur.setText("<html><font color=red>" + handler.detailContext.dstC + "</font></html>");
		}
	}
}
