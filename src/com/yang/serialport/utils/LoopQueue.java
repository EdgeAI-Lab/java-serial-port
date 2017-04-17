package com.yang.serialport.utils;

import java.util.Arrays;

public class LoopQueue {
    public int[] data = null;
    private int maxSize;
    private int rear; 
    private int front;
    private int size=0;

    private static LoopQueue mLoopQueue;

	
	
	public static LoopQueue getInstance(int initialSize){
		
		if (mLoopQueue == null) {
			mLoopQueue = new LoopQueue(initialSize);
		}
		
		return mLoopQueue;
	} 
    
    
    
    public LoopQueue() {
        this(10);
    }

    public LoopQueue(int initialSize) {
        if (initialSize >= 0) {
            this.maxSize = initialSize;
            data = new int[initialSize];
            front = rear = 0;
        } else {
            throw new RuntimeException("初始化大小不能小于0" + initialSize);
        }
    }

    // 判空
    public boolean empty() {
        return size == 0;
    }
    
    public boolean full(){
    	return size == maxSize;
    }

    // 插入
    public boolean add(int e) {
        if (size == maxSize) {
            throw new RuntimeException("队列已满，无法插入新的元素！");
        } else {
            data[rear] = e;
            rear = (rear + 1)%maxSize;
            size ++;
            return true;
        }
    }

    // 返回队首元素，但不删除
    public int peek() {
        if (empty()) {
            throw new RuntimeException("空队列异常！");
        } else {
            return data[front];
        }
    }

    // 出队
    public int poll() {
        if (empty()) {
            throw new RuntimeException("空队列异常！");
        } else {
            int value = data[front]; 
            data[front] = 0; 
            front = (front+1)%maxSize; 
            size--;
            return value;
        }
    }

    // 队列长度
    public int length() {
        return size;
    }

    // 清空循环队列
    public void clear(){
        Arrays.fill(data, 0);
        size = 0;
        front = 0;
        rear = 0;
    }
}
