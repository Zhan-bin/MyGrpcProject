package org.examplecode;

import io.grpc.*;
import org.examples.GreeterGrpc;
import org.examples.HelloReply;
import org.examples.HelloRequest;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author tanyong
 * @Version HellowordClient v1.0.0 2024/4/2 16:33 $$
 */
public class HelloWordClient {

    /**
     * 客户端对服务Greeter进行同步rpc调用 blockingStub
     */
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public HelloWordClient(Channel channel) {
        // 创建一个新的阻塞式Stub，支持对服务的一元和流输出调用
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public HelloReply greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();

        System.out.println("Send grpc message, name = " + name);
        try {
            // 设置此次RPC调用的响应超时时间为2秒
            return blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS).sayHello(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void printResp(HelloReply reply) {
        if (reply == null) {
            System.out.println("Grpc response is null.");
            return;
        }
        System.out.println("Grpc server response message: " + reply.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
        // 服务端连接地址
        List<InetSocketAddress> addressList = Arrays.asList(new InetSocketAddress("127.0.0.1", 50052),
                new InetSocketAddress("127.0.0.1", 50052));

        // 注册NameResolverProvider
        NameResolverRegistry.getDefaultRegistry().register(new LoadBalanceNameResolverProvider(addressList));

        // 符合NameResolver的有效URI
        String target = String.format("%s:///%s", HelloWordConstants.SCHEME, HelloWordConstants.SERVICE_NAME);
        System.out.println("Target URI: " + target);

        // 创建channel
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .defaultLoadBalancingPolicy("round_robin")
                // 使用明文连接到服务器。默认情况下，将使用安全连接机制，如TLS。
                // 应仅用于测试或API的使用或交换的数据不敏感的API。
                .usePlaintext()
                .build();

        try {
            HelloWordClient client = new HelloWordClient(channel);
            for (int i = 0; i < 10; i++) {
                Thread.sleep(3000);
                HelloReply reply = client.greet("user" + i);
                client.printResp(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭channel
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}


