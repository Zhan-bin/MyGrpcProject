package org.examplecode;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import org.examples.GreeterGrpc;
import org.examples.HelloReply;
import org.examples.HelloRequest;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HelloWordServer {
    private static final int port = 50052;
    private Server server;

    public void start() throws IOException {
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                // 添加业务处理类
                .addService(new GreeterImpl(port))
                .build()
                .start();

        // 注册了一个JVM关闭钩子（Shutdown Hook），当Java虚拟机（JVM）即将关闭时（无论是正常退出还是非正常退出，如接收到操作系统中断信号）当JVM关闭时，所有已注册的关闭钩子都将被依次调用
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    // 优雅地停止gRPC服务器实例
                    HelloWordServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (Objects.nonNull(server)) {
            // 发起服务器的关闭流程，不再接受新的连接和请求，但允许现有连接继续完成请求处理
            server.shutdown()
                    // 给予服务器最长30秒的时间去完成所有待处理的工作，超过这个时间限制，程序将继续执行后续逻辑，即使服务器还有任务未完成
                    // 这样设计有助于在应用退出时确保资源得到释放，同时也能防止因某些原因导致的长时间无法关闭的问题。
                    .awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * 确保主线程或者其他调用者线程会在服务器完全关闭之前保持等待状态。
     * 在主线程上等待终止，因为grpc库使用守护线程
     *
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (Objects.nonNull(server)) {
            server.awaitTermination();
        }
    }

    /**
     * 业务处理类
     */
    public static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        private final int port;

        public GreeterImpl(int port) {
            this.port = port;
        }

        /**
         * @param request
         * @param responseObserver 这是gRPC提供的响应观察者对象，用于向客户端发送响应。服务端通过调用其方法将响应数据发送给客户端。
         */
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName() + " " + this.port).build();
            try {
                Thread.sleep(500 + new Random().nextInt(1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 向客户端发送响应数据，即将创建好的 reply 对象推送给客户端。
            responseObserver.onNext(reply);
            // 表示响应已经结束，没有更多的数据要发送给客户端。
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWordServer helloWordServer = new HelloWordServer();
        helloWordServer.start();
        helloWordServer.blockUntilShutdown();
    }
}

