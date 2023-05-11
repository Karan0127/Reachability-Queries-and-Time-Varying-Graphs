#include <iostream>
#include <vector>
#include <random>
#include <math.h>
#include <fstream>

using namespace std;

int i = 0;
int N = 100;
int T = 100;
int K = 100;

fstream file;

int other(int x, int n)
{
    // Generate a random number between 1 and n
    int y = rand() % n + 1;
    if (x == y)
        return other(x, n);
    else
        return y;
}

void writeToFile(int x, int y, int t)
{
    file << x << "\t" << y << "\t" << t << endl;
}

void genCases(int N, int T, int K)
{
    for (int k = 0; k < K; k++)
    {
        int x = rand() % N + 1;
        int y = other(x, N);
        int t = rand() % T + 1;
        writeToFile(x, y, t);
    }
}

int main()
{
    string name = "./test/test" + to_string(i) + "_" + to_string(N) + "_" + to_string(T) + ".txt";
    file.open(name, ios::out);
    genCases(N, T, K);
    file.close();
    return 0;
}
