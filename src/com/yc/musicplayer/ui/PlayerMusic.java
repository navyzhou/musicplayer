package com.yc.musicplayer.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class PlayerMusic {
	// 前瞻
	private final Pattern pattern = Pattern.compile("(?<=\\[)[0-9]+\\:[0-9]+(\\.[0-9]+)?(?=\\])");
	private AudioInputStream ais;
	private AudioFormat format;
	private CLabel label_lrc; // 显示歌词的
	private CLabel label_time; // 显示播放时间的
	private ProgressBar bar; // 进度条
	private long totalTime; // 这首歌的总时长
	private long time; // 当前以播放时长
	protected static Clip clip = null;
	private Thread thread = null;
	private boolean startStatus = false; // 播放状态
	private Hashtable<Long, String> lrcs = new Hashtable<Long, String>(); // 存放歌词信息，一个时间对应一句歌词
	private List<Long> lrcTimes = new ArrayList<Long>();
	private int index = -1; // 现在显示到第几句歌词了
	private int size = 0; // 歌词的总句数
	protected static boolean playFlag = true; // 暂停或播放

	public PlayerMusic(ProgressBar bar, CLabel label_lrc, CLabel label_time) {
		this.bar = bar;
		this.label_lrc = label_lrc;
		this.label_time = label_time;
	}

	/**
	 * 播放音乐的方法
	 * @param path
	 */
	@SuppressWarnings("deprecation")
	public void start(String path) {
		if (PlayerMusic.clip != null) { // 说明有音乐正在播放
			PlayerMusic.clip.close(); // 关闭以前的音乐
			PlayerMusic.clip = null;
		}

		if (thread != null) {
			thread.stop();
		}

		bar.setSelection(0); // 进度条回到最左边

		// 读取音乐文件
		File fl = new File(path);
		if (!fl.exists() || !fl.isFile()) {
			return;
		}

		readMusicFile(fl); // 读取这个音乐文件
		readLrc(fl); // 读取歌词

		PlayerMusic.clip.start(); // 播放指定的音频文件
		thread = new Thread() {
			@Override
			public void run() {
				startStatus = true;
				index = 1;
				startStatus = true;
				boolean end = false; // 是否显示完了
				boolean mark = false; // 是否要切歌

				if (lrcs == null || lrcs.isEmpty()) {
					size = 0; // 说明没有歌词
					end = true; 
					Display.getDefault().syncExec(new Runnable() { // 匿名内部类方式
						@Override
						public void run() {
							label_lrc.setText("暂无歌词");
						}
					});
				} else {
					end = false;
					size = lrcTimes.size(); // 获取歌词的句数
					// 对歌词的时间排序
					Collections.sort(lrcTimes); // 对歌词时间升序排序
				}

				synchronized (thread) { // 说明这里面的方法同一时刻只允许一个线程方法
					while(startStatus) {
						time = PlayerMusic.clip.getMicrosecondPosition() / 1000; // 获取当前已经播放的时长

						// 显示歌词
						// 如果还有歌词没有显示完，当前歌曲播放的时间位置小于当前要显示的歌曲的时间，说明这句歌词还不需要显示
						if (!end && time < lrcTimes.get(index)) { 
							if (!mark) {
								Display.getDefault().syncExec(new Runnable() { // 匿名内部类方式
									@Override
									public void run() {
										label_lrc.setText(lrcs.get(lrcTimes.get(index - 1))); // 显示上一句歌词
									}
								});
								mark = true;
							}

						} else {
							++ index;
							if (index >= size) { // 说明所有歌词都已经显示完了
								end = true;
							}
							mark = false;
						}

						// SWT中如果想在子线程中修改界面控件，必须这样写
						Display.getDefault().syncExec(new Runnable() { // 匿名内部类方式
							@Override
							public void run() {
								label_time.setText(showTime(time));
								bar.setSelection( (int)((float)time / totalTime * 100) );
							}
						});

						if (time == totalTime) { // 说明这首歌已经播放完了
							startStatus = false;
							break;
						} else {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		thread.start(); // 启动线程 -> 会自动执行run()方法
	}

	/**
	 * 读取歌词的方法。要求歌词存放在歌曲目录中的lrc目录下，而且歌词名称必须跟歌曲名一致
	 * @param fl
	 */
	private void readLrc(File fl) {
		// 先清空原有歌词列表
		lrcs.clear();
		lrcTimes.clear();

		File file = new File(fl.getParent(), "lrc/" + fl.getName().toLowerCase().replace(".mp3", ".lrc"));
		if (!file.exists() || !file.isFile()) {
			return;
		}

		try (InputStream is = new FileInputStream(file); BufferedReader read = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
			String line = null;
			while ( (line = read.readLine()) != null ) {
				parseLine(line);
			}
			/*
			 * lrcs.forEach((key, val) -> { System.out.println(key + " : " + val); });
			 */
			// lrcTimes.forEach(System.out::println);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分离时间与歌词
	 * @param line
	 */
	private void parseLine(String line) {
		Matcher matcher = pattern.matcher(line);
		String timeStr = null;
		String str = null;
		Long times = null;
		while(matcher.find()) { // 如果根据正则表达式的规则，可以在指定的字符串中找到，则输出
			timeStr = matcher.group();
			str = line.substring(line.indexOf(timeStr) + timeStr.length() + 1);
			// 处理掉歌词中的时间
			str = str.replaceAll("^(\\[[0-9]+\\:[0-9]+(\\.[0-9]+)?\\])*", "");
			times = strToLong(timeStr);
			lrcs.put(times, str);
			lrcTimes.add(times);
		}
	}

	/**
	 * 将时间字符串转成long类型的值
	 * @param time
	 * @return
	 */
	private long strToLong(String time) {
		String[] strs = time.split(":");
		int min = Integer.parseInt(strs[0]);
		if (strs[1].contains(".")) {
			String[] temp = strs[1].split("\\.");
			int sec = Integer.parseInt(temp[0]);
			int mill = Integer.parseInt(temp[1]);
			return min * 60 * 1000 + sec * 1000 + mill * 10;
		} else {
			int sec = Integer.parseInt(strs[1]);
			return min * 60 * 1000 + sec * 1000;
		}
	}

	/**
	 * 时间处理
	 * @param time
	 * @return
	 */
	private String showTime(long time) {
		String str = "";
		long temp = 0;
		if (time < 60000) { // 如果播放时间小于1分钟
			str += "00:";
			temp = time / 1000; // 变成秒钟
			str += temp >= 10 ? String.valueOf(temp) : "0" + temp;
		} else {
			temp = time / 60000;
			str = temp >= 10 ? String.valueOf(temp) : "0" + temp;
			temp = time % 60000 / 1000;
			str += ":";
			str += temp >= 10 ? String.valueOf(temp) : "0" + temp;
		}

		str += " / ";
		if (totalTime < 60000) { // 如果播放时间小于1分钟
			str += "00:";
			temp = totalTime / 1000; // 变成秒钟
			str += temp >= 10 ? String.valueOf(temp) : "0" + temp;
		} else {
			temp = totalTime / 60000;
			str += temp >= 10 ? String.valueOf(temp) : "0" + temp;
			temp = totalTime % 60000 / 1000;
			str += ":";
			str += temp >= 10 ? String.valueOf(temp) : "0" + temp;
		}
		return str;
	}

	/**
	 * 读取音乐文件的方法
	 * @param fl
	 */
	private void readMusicFile(File fl) {
		try {
			ais = AudioSystem.getAudioInputStream(fl);
			format = ais.getFormat();

			if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, // 音频的编码格式
						format.getSampleRate(), // 每秒的样本数
						16, // 每个样本数的位数
						format.getChannels(), // 声道数
						format.getChannels() * 2,
						format.getSampleRate(),
						false);
				ais = AudioSystem.getAudioInputStream(format, ais);
			}
			PlayerMusic.clip = AudioSystem.getClip();
			PlayerMusic.clip.open(ais);

			totalTime = PlayerMusic.clip.getMicrosecondLength() / 1000;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 暂停的方法
	 */
	public void pause() {
		if (PlayerMusic.clip == null) { // 说明没有播放的音乐
			return;
		}

		PlayerMusic.clip.stop(); // 停止音乐

		// 让歌词和进度条也停止
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (thread) {
					try {
						thread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	/**
	 * 继续播放
	 */
	public void continues() {
		if (PlayerMusic.clip == null) { // 说明没有播放的音乐
			return;
		}

		PlayerMusic.clip.start(); // 停止音乐
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (thread) {
					thread.notifyAll();
				}
			}
		}).start();
	}
}
