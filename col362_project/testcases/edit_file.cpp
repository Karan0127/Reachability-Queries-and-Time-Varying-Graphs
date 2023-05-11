#include <iostream>
#include <vector>
#include <random>
#include <math.h>
#include <fstream>

using namespace std;

int main()
{
    string name = "./out.sx-superuser";
    vector<pair<int, pair<int, int> > > v;
    fstream file;
    file.open(name, ios::in);
    if (!file)
    {
        cout << "Error in opening file" << endl;
        return 0;
    }
    else
    {
        cout << "File opened successfully" << endl;
    }
    int x, y, t, ignore;
    int N = 0;
    int i = 0;
    while (file >> x >> y >> ignore >> t)
    {
        v.push_back(make_pair(t, make_pair(x, y)));
        N = max(N, max(x, y));
        if (i++ == 5000)
            break;
    }
    file.close();
    sort(v.begin(), v.end());
    
    int t_small = v[0].first;
    int t_large = v[v.size() - 1].first;

    string name2 = "./out.sorted.sx-superuser";
    file.open(name2, ios::out);
    int t_ = 1;
    for (int i = 0; i < v.size(); i++)
    {
        file << v[i].second.first << "\t" << v[i].second.second << "\t" << t_++ << endl;
    }
    cout << N << " " << t_small << " " << t_large << endl;
    cout << t_large - t_small << endl;
    cout << t_ << endl;
    file.close();
    return 0;
}
