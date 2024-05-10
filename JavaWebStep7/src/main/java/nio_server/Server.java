package nio_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;


public class Server implements Runnable {

    public static void main(String... args) throws InterruptedException {

        final int SERVER_PORT = 5050;

        Thread thread = new Thread(new Server(SERVER_PORT));
        thread.start();

        Thread.sleep(115000);
        System.out.println("Прерывание работы сервера");
        thread.interrupt();
    }

    private static final int BUFFER_SIZE = 16;
    private final int port;

    public Server(int port) {
        this.port = port;
    }


    @Override
    public void run() {
        System.out.println("Server started");

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


            System.out.println("Ожидание соединения...");
            while (!Thread.currentThread().isInterrupted()) {
                int readyChannels = selector.select();
                if (readyChannels > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                onAccept(key);
                            } else if (key.isReadable()) {
                                onRead(key);
                            } else if (key.isWritable()) {
                                onWrite(key);
                            } else {
                                System.out.println("Это. Не. Можыд. Быт.");
                            }
                        } else {
                            System.out.println("Невалидный SelectionKey");
                        }
                    }
                } else {
                    System.out.println("Нет готовых каналов");
                }
            }
            close(selector);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Работа сервера завершена");
    }

    private void onAccept(SelectionKey key) {
        try {
            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            System.out.println("Установка соединения...");
            SocketChannel socketChannel = channel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(key.selector(), SelectionKey.OP_READ);

            System.out.println("Установлено соединение с " + socketChannel.getRemoteAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void onRead(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            //System.out.println("Чтение...");

            SelectionKey selectionKey = channel.keyFor(key.selector());
            Deque<ByteBuffer> buffers = (Deque<ByteBuffer>) selectionKey.attachment();

            if (Objects.isNull(buffers)) {
                buffers = new LinkedList<>();
                System.out.println("Список буфферов создан");
                selectionKey.attach(buffers);
            }

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);


            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                System.out.println("Завершение соединения с " + channel.getRemoteAddress());
                channel.close();
            } else if (bytesRead > 0) {
                buffers.addLast(buffer);
                //System.out.println("Буффер добавлен");
                System.out.println("Прочитано " + bytesRead + " байт");
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                key.channel().close();
                System.out.println("Канал закрыт");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void onWrite(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            System.out.println("Запись...");

            SelectionKey selectionKey = channel.keyFor(key.selector());
            Deque<ByteBuffer> buffers = (Deque<ByteBuffer>) selectionKey.attachment();

            if (Objects.isNull(buffers)) {
                System.out.println("Список буфферов не создан");
                return;
            }
            if (!buffers.isEmpty()) {
                ByteBuffer buffer = buffers.pollFirst();

                buffer.flip();
                int bytesWritten = channel.write(buffer);
                System.out.println("Записано " + bytesWritten + " байт");
                buffer.compact();

                if (buffer.position() != 0) {
                    buffers.addFirst(buffer);
                    System.out.println("Буффер вернулся назаж");
                }
            }
            if (buffers.isEmpty()) {
                System.out.println("Список буфферов пуст");
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                key.channel().close();
                System.out.println("Канал закрыт");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void close(Selector selector) {
        selector.keys()
                .forEach((SelectionKey selectionKeys) -> {
                    int interestOps = selectionKeys.interestOps();
                    int keyRW = SelectionKey.OP_READ | SelectionKey.OP_WRITE; // битовое сложение

                    if ((keyRW & interestOps) > 0) {
                        try {
                            selectionKeys.channel().close();
                            System.out.println("Канал закрыт.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void closeSimple(Selector selector) {
        selector.keys().forEach(selectionKeys -> {
            try {
                selectionKeys.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }



    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Поток прерыван во время sleep(" + millis + ")");
//          e.printStackTrace();
        }
    }
}
