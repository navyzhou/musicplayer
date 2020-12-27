package com.yc.musicplayer.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * 一个规则的MP3文件大致含有3个部分: TAG_V2(ID3V2)、  Frame、 TAG_V1(ID3V1)
 * 其中TAG_V2和TAG_V1这两个部分MP3文件标签信息(歌手,歌曲名,发行时间..)保存的区域
 * 在这两个部分中,TAG_V2的长度不是固定的,包含了众多关于MP3文件的信息
 * 而TAG_V1的长度是固定的,128byte.期中包含MP3文件的基本信息.
 *  本片经验获取的MP3文件的信息就是从TAG_V1中获取的.
 */
public class ReadMusicInfo {
	/**
	 * 获取歌手信息
	 * @param fl
	 * @return 返回歌手名
	 * @throws IOException
	 */
	public static String readInfo(File fl) {
		byte[] buf = new byte[128]; // 初始化标签信息的byte数组
		try (RandomAccessFile raf = new RandomAccessFile(fl, "r")) { // 随机读写方式打开MP3文件
			raf.seek(raf.length() - 128);// 移动到文件MP3末尾
			raf.read(buf); // 读取标签信息
			raf.close(); // 关闭文件

			if(buf.length != 128){ // 数据长度是否合法
				throw new RuntimeException("MP3标签信息数据长度不合法!");
			}

			if(!"TAG".equalsIgnoreCase(new String(buf, 0, 3))){//标签头是否存在
				return "navy";
			}
			/*String songName = new String(buf, 3, 30, "GBK").trim(); //歌曲名称
	    String artist = new String(buf, 33, 30, "GBK").trim(); //歌手名字
	    String album = new String(buf, 63, 30, "GBK").trim(); //专辑名称
	    String year = new String(buf, 93, 4, "GBK").trim(); //出品年份
	    String comment = new String(buf, 97, 28, "GBK").trim();//备注信息
			 */
			return new String(buf, 33, 30, "GBK").trim();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "navy";
	}
}
