package org.examplecode;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author tanyong
 * @Version LoadBalanceNameResolver v1.0.0 2024/4/2 16:59 $$
 */
public class LoadBalanceNameResolver extends NameResolver {
    private Listener2 listener;
    private List<InetSocketAddress> socketAddressList;
    private String serviceName;
    private final Map<String, List<InetSocketAddress>> addrStore = new HashMap<>();

    public LoadBalanceNameResolver(List<InetSocketAddress> socketAddressList, String serviceName) {
        this.socketAddressList = socketAddressList;
        this.serviceName = serviceName;
        addrStore.put(serviceName, socketAddressList);
    }

    @Override
    public void start(Listener2 listener) {
        this.listener = listener;
        this.resolve();
    }

    @Override
    public void refresh() {
        this.resolve();
    }

    /**
     * 返回用于对服务器连接进行身份验证的权限。它必须来自受信任的来源，因为如果权限被篡改，rpc可能会被发送给攻击者，这可能会泄露敏感的用户数据。
     * 实现必须在不阻塞的情况下生成它，通常是在线生成，并且必须保持它不变。从同一工厂使用相同参数创建的namesolvers必须返回相同的权限。
     * 自:
     *
     * @return
     */
    @Override
    public String getServiceAuthority() {
        return "ok";
    }

    @Override
    public void shutdown() {}

    /**
     * 解析服务器地址
     */
    private void resolve() {
        List<InetSocketAddress> addresses = addrStore.get(serviceName);
        List<EquivalentAddressGroup> equivalentAddressGroup = addresses.stream().map(this::toSocketAddress)
                .map(Arrays::asList)
                .map(this::addrToEquivalentAddressGroup)
                .collect(Collectors.toList());

        ResolutionResult resolutionResult = ResolutionResult.newBuilder()
                .setAddresses(equivalentAddressGroup)
                .build();

        // 处理已解析地址和属性的更新
        this.listener.onResult(resolutionResult);

    }

    private SocketAddress toSocketAddress(InetSocketAddress address) {
        return new InetSocketAddress(address.getHostName(), address.getPort());
    }

    private EquivalentAddressGroup addrToEquivalentAddressGroup(List<SocketAddress> addrList) {
        return new EquivalentAddressGroup(addrList);
    }
}


