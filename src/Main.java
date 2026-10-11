import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService();

        System.out.println("=== 1. Customer ===");
        Customer c1 = new Customer(101, "Ivan Petrenko", "ivan@example.com");
        Customer c2 = new Customer(102, "Olena Koval", "olena@example.com");
        Customer c3 = new Customer(103, "Maksym Bondar", "max@example.com");

        System.out.println(c1);
        System.out.println(c2);
        System.out.println(c3);

        System.out.println("\n=== 2. Product ===");
        Product p1 = new Product(1, "Laptop", ProductCategory.ELECTRONICS, 28000.0);
        Product p2 = new Product(2, "Mouse", ProductCategory.ELECTRONICS, 1200.0);
        Product p3 = new Product(3, "Java Book", ProductCategory.BOOKS, 650.0);
        Product p4 = new Product(4, "Desk Lamp", ProductCategory.HOME, 900.0);

        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);

        System.out.println("\n=== 3. Order ===");
        Order o1 = new Order(10, c1, List.of(p1, p2), OrderStatus.NEW);
        Order o2 = new Order(11, c2, List.of(p3), OrderStatus.DELIVERED);
        Order o3 = new Order(12, c1, List.of(p2, p4), OrderStatus.PROCESSING);
        Order o4 = new Order(13, c3, List.of(p1, p3, p4), OrderStatus.SHIPPED);

        System.out.println(o1);
        System.out.println(o2);

        System.out.println("\n=== 4. OrderService ===");
        service.addOrder(o1);
        service.addOrder(o2);
        service.addOrder(o3);
        service.addOrder(o4);

        System.out.println("\n--- OrderService.toString() ---");
        System.out.println(service);

        System.out.println("\n--- findOrderById(11) ---");
        Order foundOrder = service.findOrderById(11);
        System.out.println(foundOrder);

        System.out.println("\n--- calculateOrderTotal(10) ---");
        System.out.println("Total: " + service.calculateOrderTotal(10));

        System.out.println("\n--- findOrdersByCustomer(101) ---");
        List<Order> customerOrders = service.findOrdersByCustomer(101);
        customerOrders.forEach(System.out::println);

        System.out.println("\n--- findOrdersByStatus(PROCESSING) ---");
        List<Order> processingOrders = service.findOrdersByStatus(OrderStatus.PROCESSING);
        processingOrders.forEach(System.out::println);

        System.out.println("\n--- findOrdersContainingProduct(2) ---");
        List<Order> ordersWithMouse = service.findOrdersContainingProduct(2);
        ordersWithMouse.forEach(System.out::println);

        System.out.println("\n--- findCustomersWithOrders() ---");
        Set<Customer> activeCustomers = service.findCustomersWithOrders();
        activeCustomers.forEach(System.out::println);

        System.out.println("\n--- sortOrdersByTotalPrice() ---");
        List<Order> sortedOrders = service.sortOrdersByTotalPrice();
        for (Order o : sortedOrders) {
            System.out.println("Total: " + service.calculateOrderTotal(o.getId()) + " | " + o);
        }

        System.out.println("\n--- countOrdersByStatus() ---");
        Map<OrderStatus, Long> statusMap = service.countOrdersByStatus();
        statusMap.forEach((status, count) -> System.out.println(status + ": " + count));

        System.out.println("\n--- removeOrder(12) ---");
        service.removeOrder(12);
        System.out.println(service);
    }
}