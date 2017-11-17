import matplotlib.pyplot as plt
from matplotlib.path import Path
import matplotlib.patches as patches


def transform_paths(raw_paths):
    paths = []
    for raw_path in raw_paths:
        points = []
        for point in raw_path.split(" -> "):
            points.append(point.split(", "))
        paths.append(points)
    return paths


def draw_path(number, name, paths, cities):
    fig = plt.figure(number)
    fig.canvas.set_window_title(name)
    ax = fig.add_subplot(111)

    x = []
    y = []
    for city in cities:
        x.append(city[0])
        y.append(city[1])

    ax.scatter(x, y, c='b')

    edgeColors = ['blue', 'red', 'green', 'orange', 'black']
    i = 0
    for path in paths:
        codes = []
        codes.append(Path.MOVETO)
        for _ in path:
            codes.append(Path.LINETO)
        del codes[-1]
        # patch = patches.Arrow(int(path[0][0]), int(path[0][1]), int(path[0][0])-int(path[1][0]), int(path[0][1])-int(path[1][1]), width=2)
        draw_path = Path(path, codes)
        patch = patches.PathPatch(draw_path, facecolor='none', edgecolor=edgeColors[i], lw=2)
        ax.add_patch(patch)
        i += 1

    ax.set_xlim(0, 2000)
    ax.set_ylim(0, 2000)
    plt.grid()


def read_solution_file(name):
    raw_paths = []
    f = open(name, 'r')
    for line in f:
        raw_paths.append(line)
    return raw_paths


def read_cities_file():
    cities = []
    f = open('cities.txt', 'r')
    for line in f:
        city = []
        for point in line.split("\t"):
            city.append(int(point))
        cities.append(city)
    return cities


cities = read_cities_file()

draw_path(1, 'Initial', transform_paths(read_solution_file('initial.txt')), cities)

draw_path(2, 'Final', transform_paths(read_solution_file('final.txt')), cities)

plt.show()
