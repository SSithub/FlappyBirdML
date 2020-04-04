package flappybirdai;

public class Experience {

    float[][] s;
    int a;
    float r;
    float[][] s_;
    boolean t;

    Experience(float[][] s, int a, float r, float[][] s_, boolean terminal) {
        this.s = s;
        this.a = a;
        this.r = r;
        this.s_ = s_;
        this.t = terminal;
    }
}
