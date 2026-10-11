import java.util.List;

public class Order {
    private long id;
    private Customer customer;
    private List<Product> products;
    private OrderStatus status;

    public Order(long id, Customer customer, List<Product> products, OrderStatus status) {
        this.id = id;
        this.customer = customer;
        this.products = products;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Product> getProducts() {
        return products;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
