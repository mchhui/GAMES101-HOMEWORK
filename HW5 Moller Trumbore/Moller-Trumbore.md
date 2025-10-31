# Möller-Trumbore算法

### 摘要

$$
\vec{O}+t\vec{D}=(1-b_1-b_2)\vec{P_0}+b_1\vec{P_1}+b_2\vec{P_2}
$$


$$
\begin{bmatrix}
t\\
b_1\\
b_2
\end{bmatrix}
=
\frac{1}{\vec{S_1}\cdot\vec{E_1}}
\begin{bmatrix}
\vec{S_2}\cdot\vec{E_2}\\
\vec{S_1}\cdot\vec{S}\\
\vec{S_2}\cdot\vec{D}
\end{bmatrix}
$$

$$
\vec{E_1}=\vec{P_1}-\vec{P_0}\\
\vec{E_2}=\vec{P_2}-\vec{P_0}\\
\vec{S}=\vec{O}-\vec{P_0}\\
\vec{S_1}=\vec{D}\times\vec{E_2}\\
\vec{S_2}=\vec{S}\times\vec{E_1}\\
其中\vec{P_n}是三角形顶点坐标向量\\ \vec{O}是射线起点坐标向量\\ \vec{D}是射线方向向量，一般取单位向量\\
$$