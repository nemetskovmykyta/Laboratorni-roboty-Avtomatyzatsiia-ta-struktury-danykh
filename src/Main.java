import java.util.ArrayList;
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

        System.out.println("Customer 1 ID: " + c1.getId());
        System.out.println("Customer 1 Name: " + c1.getName());
        System.out.println("Customer 1 Email: " + c1.getEmail());

        System.out.println("Customer toString(): " + c1.toString());
        Customer c1Duplicate = new Customer(101, "Ivan Petrenko", "ivan@example.com");
        System.out.println("c1.equals(c1Duplicate): " + c1.equals(c1Duplicate));
        System.out.println("c1 hashCode == c1Duplicate hashCode: " + (c1.hashCode() == c1Duplicate.hashCode()));

        System.out.println("\n=== 2. ProductCategory & Product ===");
        ProductCategory cat1 = ProductCategory.ELECTRONICS;
        ProductCategory cat2 = ProductCategory.BOOKS;
        ProductCategory cat3 = ProductCategory.HOME;

        Product p1 = new Product(1, "Laptop", cat1, 28000.0);
        Product p2 = new Product(2, "Mouse", ProductCategory.ELECTRONICS, 1200.0);
        Product p3 = new Product(3, "Java Book", cat2, 650.0);
        Product p4 = new Product(4, "Desk Lamp", cat3, 900.0);

        System.out.println("Product 1 ID: " + p1.getId());
        System.out.println("Product 1 Name: " + p1.getName());
        System.out.println("Product 1 Category: " + p1.getCategory());
        System.out.println("Product 1 Price: " + p1.getPrice());

        System.out.println("Product toString(): " + p1.toString());
        Product p1Duplicate = new Product(1, "Laptop", cat1, 28000.0);
        System.out.println("p1.equals(p1Duplicate): " + p1.equals(p1Duplicate));
        System.out.println("p1 hashCode == p1Duplicate hashCode: " + (p1.hashCode() == p1Duplicate.hashCode()));

        System.out.println("\n=== 3. OrderStatus & Order ===");
        List<Product> list1 = new ArrayList<>();
        list1.add(p1);
        list1.add(p2);

        List<Product> list2 = new ArrayList<>();
        list2.add(p3);

        List<Product> list3 = new ArrayList<>();
        list3.add(p2);
        list3.add(p4);

        List<Product> list4 = new ArrayList<>();
        list4.add(p1);
        list4.add(p3);
        list4.add(p4);

        OrderStatus statusNew = OrderStatus.NEW;
        OrderStatus statusDelivered = OrderStatus.DELIVERED;
        OrderStatus statusProcessing = OrderStatus.PROCESSING;
        OrderStatus statusShipped = OrderStatus.SHIPPED;

        Order o1 = new Order(10, c1, list1, statusNew);
        Order o2 = new Order(11, c2, list2, statusDelivered);
        Order o3 = new Order(12, c1, list3, statusProcessing);
        Order o4 = new Order(13, c3, list4, statusShipped);

        System.out.println("Order 1 ID: " + o1.getId());
        System.out.println("Order 1 Customer: " + o1.getCustomer().getName());
        System.out.println("Order 1 Products count: " + o1.getProducts().size());
        System.out.println("Order 1 Status (before): " + o1.getStatus());

        o1.setStatus(OrderStatus.CANCELLED);
        System.out.println("Order 1 Status (after setStatus): " + o1.getStatus());
        o1.setStatus(OrderStatus.NEW);

        System.out.println("Order toString(): " + o1.toString());

        System.out.println("\n=== 4. OrderService ===");
        service.addOrder(o1);
        service.addOrder(o2);
        service.addOrder(o3);
        service.addOrder(o4);

        System.out.println("\n--- findOrderById(11) ---");
        Order foundOrder = service.findOrderById(11);
        System.out.println("Found: " + foundOrder);

        System.out.println("\n--- calculateOrderTotal(10) ---");
        double total10 = service.calculateOrderTotal(10);
        System.out.println("Total ID 10: " + total10);

        System.out.println("\n--- findOrdersByCustomer(101) ---");
        List<Order> customerOrders = service.findOrdersByCustomer(101);
        for (Order o : customerOrders) {
            System.out.println(o);
        }

        System.out.println("\n--- findOrdersByStatus(PROCESSING) ---");
        List<Order> processingOrders = service.findOrdersByStatus(OrderStatus.PROCESSING);
        for (Order o : processingOrders) {
            System.out.println(o);
        }

        System.out.println("\n--- findOrdersContainingProduct(2) ---");
        List<Order> ordersWithMouse = service.findOrdersContainingProduct(2);
        for (Order o : ordersWithMouse) {
            System.out.println(o);
        }

        System.out.println("\n--- findCustomersWithOrders() ---");
        Set<Customer> activeCustomers = service.findCustomersWithOrders();
        for (Customer c : activeCustomers) {
            System.out.println(c);
        }

        System.out.println("\n--- sortOrdersByTotalPrice() ---");
        List<Order> sortedOrders = service.sortOrdersByTotalPrice();
        for (Order o : sortedOrders) {
            double price = service.calculateOrderTotal(o.getId());
            System.out.println("Order ID: " + o.getId() + " | Total: " + price);
        }

        System.out.println("\n--- countOrdersByStatus() ---");
        Map<OrderStatus, Long> statusMap = service.countOrdersByStatus();
        for (Map.Entry<OrderStatus, Long> entry : statusMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\n--- removeOrder(12) ---");
        System.out.println("Before remove: " + service.findOrderById(12));
        service.removeOrder(12);
        System.out.println("After remove: " + service.findOrderById(12));
    }
}