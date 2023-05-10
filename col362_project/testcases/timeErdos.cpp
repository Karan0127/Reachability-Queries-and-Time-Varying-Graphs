#include <iostream>
#include <vector>
#include <random>
#include <math.h>
#include <fstream>

using namespace std;

int i = 0;
int N = 100;
int T = 100;
int a = 0;
int b = 10;
int c = 10;
float k = 1.0 / c;

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
    file << x << " " << y << " " << t << endl;
}

void timeErdos(int N, int T)
{
    for (int t = 1; t <= T; t++)
    {
        // generate a random float between 0 and 1
        float r = static_cast <float> (rand()) / static_cast <float> (RAND_MAX);

        int edges = (int) ( floor(a + (b - a + 1) * (1.0 - pow(r, k))) );
        for (int i = 0; i < edges; i++)
        {
            int x = rand() % N + 1;
            int y = other(x, N);
            writeToFile(x, y, t);
        }

    }
}

int main()
{
    string name = "./generated/file" + to_string(i) + "_" + to_string(N) + "_" + to_string(T) + "_" + to_string(c) + ".txt";
    file.open(name, ios::out | ios::app);
    timeErdos(N, T);
    file.close();
    return 0;
}
