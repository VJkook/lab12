package org.example;

import org.hibernate.Session;

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;

import java.util.Random;
import java.util.concurrent.CountDownLatch;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        Random rnd = new Random();
        Session session = СonnectionHiber.getSessionFactory().openSession();
        session.beginTransaction();
        for (int i = 0; i < 40; i++) {
            BigItem bigItem = new BigItem();
            session.save(bigItem);
        }
        session.getTransaction().commit();
        long startTime = System.currentTimeMillis();
        //Optimistic();
        Pessimistic();
        long endTime= System.currentTimeMillis()-startTime;
        System.out.println("Время выполнения: "+ endTime);

        Sum();
    }
    public  static  void Optimistic(){
        CountDownLatch cdl = new CountDownLatch(8);
        for(int i = 0; i < 8; i++){
            new Thread(() -> {
                Session session = СonnectionHiber.getSessionFactory().openSession();

                for(int j = 0;j < 20_000; j++){
                    try {
                        session.beginTransaction();
                        Long t = (int) (Math.random() * 41) / 1L;
                        if (t == 0L) {
                            t = 1L;
                        }
                        BigItem bigItem = session.get(BigItem.class, t);
                        bigItem.setVal(bigItem.getVal() + 1);
                        session.save(bigItem);
                        session.getTransaction().commit();
                    }
                    catch (OptimisticLockException a){
                        session.getTransaction().rollback();
                        j--;
                    }
                    UncheckableSleep(3);
                }
//                System.out.println("Thread ready");
                session.close();
                cdl.countDown();
            }).start();
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void  Pessimistic(){
        CountDownLatch cdl_2 = new CountDownLatch(8);
        for(int i = 0; i < 8; i++){
            new Thread(() -> {
                Session session_2 = СonnectionHiber.getSessionFactory().openSession();

                for(int j = 0;j < 20000; j++){
                    try {
                        session_2.beginTransaction();
                        Long t = (int) (Math.random() * 41) / 1L;
                        if (t == 0L) {
                            t = 1L;
                        }
                        BigItem bigItem = session_2
                                .createQuery("from BigItem item where id ="+t, BigItem.class)
                                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                                .getSingleResult();
                        bigItem.setVal(bigItem.getVal() + 1);
                        session_2.save(bigItem);
                        session_2.getTransaction().commit();
                    }
                    catch (OptimisticLockException a){
                        session_2.getTransaction().rollback();
                        j--;
                    }
                    UncheckableSleep(3);
                }
//              System.out.println("Thread ready");
                session_2.close();
                cdl_2.countDown();
            }).start();
        }
        try {
            cdl_2.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void Sum(){
        Session s= СonnectionHiber.getSessionFactory().openSession();
        s.beginTransaction();
        Object o=s.createNativeQuery("select sum(val) from big_items").getSingleResult();
        System.out.println("Сумма всех обьектов " + o);
        s.close();
    }
    public static void UncheckableSleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
