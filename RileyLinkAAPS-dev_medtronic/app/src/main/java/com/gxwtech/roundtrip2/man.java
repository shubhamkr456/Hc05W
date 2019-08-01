

import java.lang.Math;
import java.util.ArrayList;

public class man {
    double b;
    double c;
    double f;
    double gb;
    double k21;
    double kabs;
    double kg;
    double kmax;
    double kmin;
    double kx;
    double p1;
    double p2;
    double si;
    double ka1;
    double ka2;
    double kd;
    double ke;
    double ki;
    double tau;
    double bg;
    double alpha;
    double beta;
    double kempt;
    double p3;
    double states[]={0,0,0,110,0,0,381.988,76.154,0.00783015,130};

    public static void main(String args[])
    {
        man d2 = new man(60.4,0.85264,0.083783,0.98904,253.06,0.021936,0.20827,0.13313,0.063483,0.019775,0.00022993,0.010017,0.010027,0.00020492,0.0078994,0.0094409,0.0031099,0.0062476,0.58884,7.862);
        double x0 = 0, y = 0, x = 4, h = 0.2;
        double states[]={0,0,0,110,0,0,381.988,76.154,0.00783015,130};
        double states1[]=d2.rungeKutta(x0, states, x, h);
        System.out.println("\nThe value of y at x is : ");
        for (int i=0;i<10;i++){
            //System.out.println(states1[i]);
        }

    }
    public man( double bg,double b,double c,double f,double gb,double k21,double kabs,double kg,double kmax,double kmin,double kx,double p1,double p2,double si,double ka1,double ka2,double kd,double ke,double ki,double tau){
        this.b=b;
        this.bg=bg;
        this.c=c;
        this.f=f;
        this.gb=gb;
        this.k21=k21;
        this.kempt=(kmin + ((kmax - kmin) / 2) * (Math.tanh(alpha * (-b * 40000)) - Math.tanh(beta * (-c * 40000))));
        this.kabs=kabs;
        this.kg=kg;
        this.kmax=kmax;
        this.kmin=kmin;
        this.kx=kx;
        this.p1=p1;
        this.p2=p2;
        this.si=si;
        this.p3=si*p2;
        this.ka1=ka1;
        this.ka2=ka2;
        this.kd=kd;
        this.ke=ke;
        this.ki=ki;
        this.tau=tau;
        this.alpha=5/(2*4*(1-b));
        this.beta=5/(2*4*c);
    }


    //        double sto1(double x, double y,double states[]) {
////        if (x==0){
////            return 0;
////        }
//            if (x < 2 && x >= 2) {
//                return -k21 * states[0];
//            } else {
//                return -k21 * states[0] + 40;
//            }
//        }
//
//        double sto2(double x, double y) {
////            if(x==0){
////                return 0;
////            }
//            this.kempt = kempt(x, y);
//            return (-kempt * y + k21 * sto1(x, y));
//
//        }
//
//        double kempt(double x, double y) {
//            if (x == 0)
//                return kempt;
//            else {
//                this.kempt = (kmin + ((kmax - kmin) / 2) * (Math.tanh(alpha * (sto1(x, y) + sto2(x, y) - b * 40000)) - Math.tanh(beta * (sto1(x, y) + sto2(x, y) - c * 40))));
//
//                return kempt;
//            }
//        }
//
//        double gut(double x, double y) {
//            return -kabs * y + kempt * sto2(x, y);
//        }
//
//        double Ug(double x, double y) {
//            return f * kabs * gut(x, y);
//        }
//
//
//        double Us(double x, double y) {
//
//            return 0.5 * (ig(x, y) - 70);
//        }
//
//        double ig(double x, double y){
//
//            return -y/tau + GB(x,y)/tau;
//        }
//
//        double x(double x, double y) {
//
//            return -p2*y+p3*(it(x,y));
//        }
//        double GB(double x, double y){
//            return (-p1-x(x,y))*y+p1*gb+Ug(x,y)/kg*bg;
//        }
//
//        double isc1(double x, double y){
//
//            return -(ka1+kd)*y +Us(x,y);
//        }
//        double isc2(double x,double y){
//            return -ka2*y+kd*isc1(x,y);
//        }
//        double ip(double x, double y){
//            return -ke*y+ka1*isc1(x,y)+ka2*isc2(x,y);
//        }
//        double it(double x, double y){
//            return ip(x,y)/(ki*bg);
//        }
    double[] rungeKutta(double x0, double[] y0, double x, double h)
    {
        // Count number of iterations using step size or
        // step height h
        int n = (int)((x - x0) / h);


        // Iterate for number of iterations
        for (int i=0;i<10;i++){
            states[i]=y0[i];
        }
        for (int i = 1; i <= n; i++) {
            // Apply Runge Kutta Formulas to find
            // next value of y
            double k1a[]=new double[10];
            double k2a[]=new double[10];
            double k3a[]=new double[10];
            double k4a[]=new double[10];
            double data1[]=new double[10];
            double data2[]=new double[10];
            double data3[]=new double[10];
            double k1b[]=fstates(x0,states);
            for(int j=0;j<10;++j)
                k1a[j]=h*k1b[j];
            for(int k=0;k<10;++k){
                data1[k]=states[k]+0.5*k1a[k];
            }
            double k2b[]=fstates(x0 + 0.5 * h,states);
            for(int j=0;j<10;++j) {
                k2a[j] = h * k2b[j];
            }
            for(int k=0;k<10;++k){
                data2[k]=states[k]+0.5*k2a[k];
            }
            double k3b[]=fstates(x0+0.5*h,states);
            for(int j=0;j<10;++j) {
                k3a[j] = h * k3b[j];
            }
            for(int k=0;k<10;++k){
                data3[k]=states[k]+k3a[k];
            }
            double k4b[]=fstates(x0+h,states);
            for(int j=0;j<10;++j) {
                k4a[j] = h * k4b[j];
            }
            for(int k=0;k<10;++k){
                states[k]=states[k]+(1.0 / 6.0) * (k1a[k] + 2 * k2a[k] + 2 * k3a[k] + k4a[k]);
            }



            // k2 = h * (GB(x0 + 0.5 * h, y + 0.5 * k1));
            //k3 = h * (GB(x0 + 0.5 * h, y + 0.5 * k2));
            //k4 = h * (GB(x0 + h, y + k3));

            // Update next value of y
            // y = y + (1.0 / 6.0) * (k1 + 2 * k2 + 2 * k3 + k4);

            // Update next value of x
            x0 = x0 + h;
            String text="";
            for(int m=0;m<10;m++){
                text=text+" "+String.valueOf(states[m]);
            }
            System.out.println(text);

        }

        return states;
    }
    double[] fstates(double x,double states[]) {
        double data[]=new double[10];
        double kempt = (kmin + ((kmax - kmin) / 2) * (Math.tanh(alpha * ((states[0] + states[1]) - (b * 80000))) - Math.tanh(beta * ((states[0] + states[1]) - (c * 80000)))));
        System.out.println(kempt);
        if (x < 0 && x >= 2) {
            data[0] = -k21 * states[0];

        } else {
            data[0] = -k21 * states[0] + 40000;
        }
        data[1] = (-kempt * states[1] + k21 * states[0]);
        data[2] = -kabs * states[2] + kempt * states[1];
        double Ug=f*kabs*states[2];
        double us;
        if(x<=(1/6)){
            us=72000;
        }
        else{
            us=0;
        }
        data[3]=-p1*(states[3]-253.06)-states[8]*states[3]+(Ug/(bg*kg));
        data[4]=-(ka1+kd)*states[4]+us;
        data[5]=-ka2*states[5]+kd*states[4];
        data[6]=-ke*states[6]+ka1*states[4]+ka2*states[5];
        data[7]=states[6]/(ki*bg);
        data[8]=-p2*states[8]+p3*states[7];
        data[9]=(-states[9]+states[3])/tau;
        // System.out.println(data[0]+" "+data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5]+" "+data[6]+" "+data[7]+" "data[8]+" "+data[9]);
        // System.out.println(data[0]+" "+data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5]+" "+data[6]+" "+data[7]+" "data[8]+" "+data[9]);

        return data;
    }



}
