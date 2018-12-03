package com.example.ht.d2d_one.icn;

/**
 *created by ht at 2018/11/29
 */
public class DoubleLinkedList {
    Node head =new  Node ("header","header");
    Node rear = head;
    public Node get(String limitationKey) {
        Node result = head;
        while(!result.next.key.equals(limitationKey)) {
            result = result.next;
            if(result ==head) {
                System.out.println("查无此key值对应的项，key="+limitationKey);
                break;
            }
        }
        result = result.next;
        return result;
    }
    public void put(String key,String value) {
        Node newNode = new Node(key,value);
        rear.next = newNode;
        head.pre = newNode;
        newNode.next = head;
        newNode.pre = rear;
        rear = newNode;
    }
    public void clear(){
        head.next = null;
        head.pre = null;
        rear = head;
    }
    public void update(String limitationKey) {
        System.out.println(head.key);
        System.out.println(head.next.key);
        Node aimNode = get(limitationKey);
        Node temp = head.next;
        head.next = aimNode;
        temp.pre = null;
        temp = aimNode.pre;
        aimNode.pre = head;
        temp.next = null;
        temp = null;
        System.out.println("hhhhh"+head.next.key);
    }
    public void show(Node limitaionNode) {
        Node temp = limitaionNode;
        while(temp != head) {
            System.out.println(temp.value);
            temp = temp.next;
        }
    }
    public static class Node{
        String key;
        String value;
        Node next;
        Node pre;
        public Node(String key,String value){
            this.key = key;
            this.value = value;
        }
    }
}