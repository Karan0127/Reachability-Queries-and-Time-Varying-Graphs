#include <iostream>
#include <vector>
#include <random>
#include <math.h>
#include <fstream>

using namespace std;

int main()
{
    string name = "./out.sorted.sx-superuser";
    vector<pair<pair<int, int>, int > > v;
    fstream file;
    file.open(name, ios::in);
    int x, y, t;
    while (file >> x >> y >> t)
    {
        v.push_back(make_pair(make_pair(x, y), t));
    }
    file.close();
    sort(v.begin(), v.end());
    

    string name2 = "./for_baseline";
    file.open(name2, ios::out);
    for (int i = 0; i < v.size(); i++)
    {
        file << v[i].first.first << "\t" << v[i].first.second << "\t" << v[i].second << endl;
    }
    file.close();
    return 0;
}
