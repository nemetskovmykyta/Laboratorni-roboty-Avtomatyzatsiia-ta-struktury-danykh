import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderService {
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(long orderId) {
        Order target = findOrderById(orderId);
        if (target != null) {
            orders.remove(target);
        }
    }

    public Order findOrderById(long orderId) {
        for (Order o : orders) {
            if (o.getId() == orderId) {
                return o;
            }
        }
        return null;
    }

    public List<Order> findOrdersByCustomer(long customerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getCustomer().getId() == customerId) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() == status) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> findOrdersContainingProduct(long productId) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            for (Product p : o.getProducts()) {
                if (p.getId() == productId) {
                    result.add(o);
                    break;
                }
            }
        }
        return result;
    }

    public double calculateOrderTotal(long orderId) {
        Order o = findOrderById(orderId);
        if (o == null) {
            return 0.0;
        }

        double total = 0;
        for (Product p : o.getProducts()) {
            total += p.getPrice();
        }
        return total;
    }

    public Set<Customer> findCustomersWithOrders() {
        Set<Customer> result = new HashSet<>();
        for (Order o : orders) {
            result.add(o.getCustomer());
        }
        return result;
    }

    public List<Order> sortOrdersByTotalPrice() {
        List<Order> sortedList = new ArrayList<>(orders);
        for (int i = 0; i < sortedList.size() - 1; i++) {
            for (int j = 0; j < sortedList.size() - 1 - i; j++) {
                double total1 = calculateOrderTotal(sortedList.get(j).getId());
                double total2 = calculateOrderTotal(sortedList.get(j + 1).getId());
                if (total1 > total2) {
                    Order temp = sortedList.get(j);
                    sortedList.set(j, sortedList.get(j + 1));
                    sortedList.set(j + 1, temp);
                }
            }
        }
        return sortedList;
    }

    public Map<OrderStatus, Long> countOrdersByStatus() {
        Map<OrderStatus, Long> map = new HashMap<>();
        for (Order o : orders) {
            OrderStatus s = o.getStatus();
            map.put(s, map.getOrDefault(s, 0L) + 1);
        }
        return map;
    }
}
