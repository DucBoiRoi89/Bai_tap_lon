public class BidTransaction {
    private User user;
    private double amount;
    private long time;
    public BidTransaction(User user , double amount ){
        this.user = user;
        this.amount =amount  ;
        time = System.currentTimeMillis();

    }
    public User getUser() {
        return user;
    }

    public double getAmount() {
        return amount;
    }

    public long getTime() {
        return time;
    }
    @Override
    public String toString(){
        return user +" "+ amount;

    }


}
