package com.yc.musicplayer.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MainPlayerUI {
	protected Shell shell;
	private int clickX;
	private int clickY;
	private boolean checkDown = false;
	private Table table;
	private boolean isOrder = true; // 顺序播放

	// 播放列表
	private Map<String, String> musicList = new HashMap<String, String>();
	private PlayerMusic playerMusic; // 播放音乐的对象

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainPlayerUI window = new MainPlayerUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.NONE);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setBackgroundImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/11.jpg"));
		shell.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/yc.png"));
		shell.setSize(1200, 700);
		shell.setText("源辰-音乐播放器");

		// 居中显示界面
		Rectangle rect = Display.getDefault().getClientArea();
		shell.setLocation((rect.width - shell.getSize().x) / 2, (rect.height - shell.getSize().y) / 2);
		shell.setLayout(new FormLayout());

		CLabel label_close = new CLabel(shell, SWT.NONE);
		FormData fd_label_close = new FormData();
		fd_label_close.right = new FormAttachment(100);
		fd_label_close.bottom = new FormAttachment(0, 42);
		label_close.setLayoutData(fd_label_close);
		label_close.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/close_3.png"));
		label_close.setText("");

		Composite composite = new Composite(shell, SWT.BORDER);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 698);
		fd_composite.right = new FormAttachment(0, 158);
		fd_composite.top = new FormAttachment(0, 45);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);

		Composite composite_1 = new Composite(shell, SWT.BORDER);
		FormData fd_composite_1 = new FormData();
		fd_composite_1.top = new FormAttachment(label_close, 3);
		fd_composite_1.right = new FormAttachment(label_close, 0, SWT.RIGHT);
		fd_composite_1.left = new FormAttachment(0, 160);
		fd_composite_1.bottom = new FormAttachment(100, -95);
		composite_1.setLayoutData(fd_composite_1);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

		table = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(415);
		tblclmnNewColumn.setText("    歌曲");

		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(411);
		tblclmnNewColumn_1.setText("  歌手");

		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(202);
		tblclmnNewColumn_2.setText("  大小");


		ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.top = new FormAttachment(label_close, 560);
		fd_progressBar.right = new FormAttachment(label_close, 0, SWT.RIGHT);
		fd_progressBar.left = new FormAttachment(0, 160);
		progressBar.setLayoutData(fd_progressBar);

		Composite composite_2 = new Composite(shell, SWT.BORDER);
		FormData fd_composite_2 = new FormData();
		fd_composite_2.right = new FormAttachment(label_close, 0, SWT.RIGHT);
		fd_composite_2.bottom = new FormAttachment(composite, 0, SWT.BOTTOM);
		fd_composite_2.top = new FormAttachment(0, 620);
		fd_composite_2.left = new FormAttachment(0, 160);
		composite_2.setLayoutData(fd_composite_2);

		CLabel label_lrc = new CLabel(composite_2, SWT.NONE);
		label_lrc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		label_lrc.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		label_lrc.setBounds(0, 17, 610, 42);

		CLabel label_time = new CLabel(composite_2, SWT.NONE);
		label_time.setText("");
		label_time.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		label_time.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.BOLD));
		label_time.setBounds(904, 17, 130, 42);

		Composite composite_3 = new Composite(shell, SWT.NONE);
		fd_label_close.top = new FormAttachment(composite_3, 0, SWT.TOP);
		FormData fd_composite_3 = new FormData();
		fd_composite_3.bottom = new FormAttachment(0, 42);
		fd_composite_3.right = new FormAttachment(0, 624);
		fd_composite_3.top = new FormAttachment(0);
		fd_composite_3.left = new FormAttachment(0);
		composite_3.setLayoutData(fd_composite_3);
		composite_3.setBackgroundMode(SWT.INHERIT_DEFAULT);

		ToolBar toolBar = new ToolBar(composite_3, SWT.FLAT | SWT.RIGHT);
		toolBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		toolBar.setBounds(164, 2, 40, 38);

		ToolItem item = new ToolItem(toolBar, SWT.NONE);
		item.setToolTipText("添加歌曲");
		item.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/add.png"));

		playerMusic = new PlayerMusic(progressBar, label_lrc, label_time);

		ToolBar toolBar_1 = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
		toolBar_1.setBounds(750, 10, 55, 54);

		ToolItem item_2 = new ToolItem(toolBar_1, SWT.NONE);
		item_2.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/play_3.png"));
		item_2.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/timeout_2.png"));

		ToolBar toolBar_2 = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
		toolBar_2.setBounds(807, 17, 40, 40);

		ToolItem item_3 = new ToolItem(toolBar_2, SWT.NONE);
		item_3.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/nextsong_1.png"));
		item_3.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/nextsong_2.png"));

		ToolBar toolBar_3 = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
		toolBar_3.setBounds(708, 17, 40, 40);

		ToolItem item_4 = new ToolItem(toolBar_3, SWT.NONE);
		item_4.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/previous_1.png"));
		item_4.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/previous_2.png"));

		ToolBar toolBar_4 = new ToolBar(composite_2, SWT.FLAT | SWT.RIGHT);
		toolBar_4.setBounds(665, 17, 40, 40);

		ToolItem item_5 = new ToolItem(toolBar_4, SWT.NONE);
		item_5.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/random_2.png"));
		item_5.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/refresh.png"));

		// 下一首
		item_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = 0;
				if (isOrder) {
					index = table.getSelectionIndex(); // 获取当前播放的歌曲的索引下标
					++ index; // 获取下一首的索引下标
					index = index % table.getItemCount();
				} else {
					Random rd = new Random();
					index = rd.nextInt(table.getItemCount()); // [0, table.getItemCount())
				}
				table.setSelection(index);
				playerMusic.start( musicList.get(table.getItem(index).getText(0).trim())); 
			}
		});

		// 上一首
		item_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = 0;
				if (isOrder) {
					index = table.getSelectionIndex(); // 获取当前播放的歌曲的索引下标
					-- index; // 获取下一首的索引下标
					if (index < 0) {
						index = table.getItemCount() - 1;
					}
				} else {
					Random rd = new Random();
					index = rd.nextInt(table.getItemCount()); // [0, table.getItemCount())
				}
				table.setSelection(index);
				playerMusic.start( musicList.get(table.getItem(index).getText(0).trim())); 	
			}
		});

		// 暂停播放
		item_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (PlayerMusic.playFlag) { // 说正在播放
					PlayerMusic.playFlag = false; // 说明要暂停
					item_2.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/timeout_2.png"));
					item_2.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/play_3.png"));
					playerMusic.pause();
				} else {
					PlayerMusic.playFlag = true;
					item_2.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/play_3.png"));
					item_2.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/timeout_2.png"));
					playerMusic.continues();
				}
			}
		});

		// 循环和随机
		item_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isOrder) { // 说正在播放
					isOrder = false; // 说明要暂停
					item_5.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/refresh.png"));
					item_5.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/random_2.png"));
				} else {
					isOrder = true;
					item_5.setHotImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/random_2.png"));
					item_5.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/refresh.png"));
				}
			}
		});

		// 双击播放音乐
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] tis = table.getSelection(); // 获取表格中所有选中的行
				if (tis == null || tis.length <= 0) {
					return;
				}
				label_lrc.setText(tis[0].getText(0).trim());
				String path = musicList.get(tis[0].getText(0).trim()); // 根据歌曲名从map中取出这个首歌的绝对路径
				playerMusic.start(path); // 调用播放音乐的方法，播放音乐
			}
		});

		// 点击添加歌曲
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 弹出一个路径选择框
				DirectoryDialog dd = new DirectoryDialog(shell, SWT.SELECTED | SWT.OPEN);
				dd.setText("路径选择");
				dd.setMessage("请选择您要导入的音乐所在的目录");
				String path = dd.open();

				findMp3(path);
			}
		});

		// 关闭按钮的事件监听
		label_close.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) { // 当鼠标移动到这个标签上时
				label_close.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/close_1.png"));
			}
			@Override
			public void mouseExit(MouseEvent e) { // 当鼠标从这个标签上移开时
				label_close.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/close_3.png"));
			}
		});
		label_close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) { // 当鼠标在这个标签上按下时
				label_close.setImage(SWTResourceManager.getImage(MainPlayerUI.class, "/images/close_2.png"));
			}
			@Override
			public void mouseUp(MouseEvent e) { // 当鼠标在这个标签上松开时
				if (!MessageDialog.openConfirm(shell, "确认提示", "不再听一会儿...")) {
					return;
				}

				shell.dispose();
				System.exit(0);
			}
		});

		// 设置光标样式
		Cursor cursor = new Cursor(Display.getDefault(), SWTResourceManager.getImage(MainPlayerUI.class, "/images/move.png").getImageData(), 16, 16);

		// 界面拖动
		composite_3.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (checkDown)  {
					shell.setLocation((shell.getLocation().x + e.x - clickX), (shell.getLocation().y + e.y - clickY));
				}
			}
		});

		composite_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) { // 记录鼠标按下去时的坐标位置
				checkDown = true;
				clickX = e.x;
				clickY = e.y;
				shell.setCursor(cursor); // 设置光标样式
			}
			@Override
			public void mouseUp(MouseEvent e) {
				checkDown = false;
				shell.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_ARROW));
			}
		});

		findMp3("musics");
		table.setSelection(0);
		playerMusic.start( musicList.get(table.getItem(0).getText(0).trim())); // 默认播放第一首歌
	}

	/**
	 * 检索指定路径下的所有mp3
	 * @param path
	 */
	private void findMp3(String path) {
		if (path == null || "".equals(path)) {
			return;
		}

		File fl = new File(path);
		if (!fl.exists() || !fl.isDirectory()) {
			return;
		}

		// 获取这个目录下的所有子目录和文件
		File[] files = fl.listFiles();
		if (files == null || files.length <= 0) {
			return;
		}

		String fileName = null;
		TableItem ti = null; // 行对象
		DecimalFormat df = new DecimalFormat("00.00");
		// 循环所有文件，判断是不是mp3
		for (File f : files) {
			fileName = f.getName().toLowerCase();

			// 如果当前File是一个文件，并且是一.mp3结尾，说明是我想要的MP3文件
			if (f.isFile() && fileName.endsWith(".mp3")) {
				ti = new TableItem(table, SWT.NONE);
				ti.setText(new String[]{fileName, ReadMusicInfo.readInfo(f), df.format(f.length() / 1024.0 / 1024.0)});
				musicList.put(fileName, f.getAbsolutePath());
			}
		}
	}
}
