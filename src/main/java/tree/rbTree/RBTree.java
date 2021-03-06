package tree.rbTree;

/**
 * @author Jzedy
 * @date 2019/3/5
 */
public class RBTree<T extends Comparable<T>> {

    private Node<T> root;

    private static final boolean RED = true;
    private static final boolean BLACK = false;


    class Node<T>{
        private T key;
        private Node<T> left;//左子节点
        private Node<T> right;//右子节点
        private Node<T> parent;//父节点
        private boolean color;//颜色

        public Node(T key, Node<T> parent) {
            this.key = key;
            this.parent = parent;
        }
    }

    private Node<T> parentOf(Node<T> node){
        return node == null ? null : node.parent;
    }

    private Node<T> leftOf(Node<T> node){
        return node == null ? null : node.left;
    }

    private Node<T> rightOf(Node<T> node){
        return node == null ? null : node.right;
    }

    private boolean colorOf(Node<T> node){
        return node == null ? BLACK : node.color;
    }

    private void setColor(Node<T> node,boolean color){
        if (node != null){
            node.color = color;
        }
    }

    /**
     * 左旋(对x左旋) 下图为x为父节点的左子节点
     *        px                             px
     *       /                              /
     *     x             ==>              y
     *   / \                            / \
     * lx   y                         x   ry
     *    / \                       / \
     *  ly  ry                    lx  ly
     * @param x
     */
    private void leftRotation(Node<T> x){
        if (x != null){
            Node<T> y = x.right;
            x.right = y.left;
            if (y.left != null){
                y.left.parent = x;
            }
            y.parent = x.parent;
            if (x.parent == null){
                root = y;
            }else if (x.parent.left == x){
                x.parent.left = y;
            }else {
                x.parent.right = y;
            }
            x.parent = y;
            y.left = x;
        }
    }

    /**
     *右旋(对x右旋) 下图为x为父节点的左子节点
     *             px                              px
     *           /                                /
     *         x                                y
     *       / \                             /  \
     *     y   rx          ==>             ly   x
     *   / \                                  / \
     * ly  ry                               ry  rx
     *
     * @param x
     */
    private void rightRotation(Node<T> x){
        if (x != null){
            Node<T> y = x.left;
            x.left = y.right;
            if (y.right != null){
                y.right.parent = x;
            }
            y.parent = x.parent;
            if (x.parent == null){
                root = y;
            }else if (x.parent.left == x){
                x.parent.left = y;
            }else {
                x.parent.right = y;
            }
            y.right = x;
            x.parent = y;
        }
    }

    public boolean add(T key){
        Node<T> current = root;
        if (current == null){
            root = new Node<>(key,null);
            return true;
        }

        Node<T> parent;int cmp;
        do {
            parent = current;
            cmp = key.compareTo(current.key);
            if (cmp < 0){
                current = current.left;
            }else if (cmp > 0){
                current = current.right;
            }else return false;//红黑树中已经有对应的节点
        }while (current != null);
        Node<T> e = new Node<>(key,parent);
        if (cmp < 0){
            parent.left = e;
        }else parent.right = e;
        fixAfterAdd(e);
        return true;
    }

    /**
     * 当前添加节点颜色设置为红色。
     * 若父节点为空，表示当前添加节点为根节点，将根节点置为黑色就可以了
     * 若父节点不为空，只有当父节点颜色为红色时候才调整结构
     * 根据对称性，描述父节点为祖父节点的左子节点的情况，当父节点为祖父节点的右子节点时候，根据对称性，左旋/右旋相反操作即可
     * 当父节点为祖父节点左子节点且父节点为红色时候，有如下几种情况：
     * 1.叔叔节点也是红色
     * 2.叔叔节点是黑色
     *
     * 先讨论叔叔节点是黑色时候，又有下面两种情况：
     * 2.1 添加节点是父节点的左子节点
     * 2.2 添加节点是父节点的右子节点
     *
     * 先看添加节点是父节点的左子节点：
     * 2.1 让父节点颜色为红，祖父节点为黑 ，祖父节点右旋
     *
     *         gx(b)
     *        /   \           x为添加节点    px(b)
     *     px(r)  uncle(b)       ==>         /   \
     *    /                             x(r)   gx(r)
     * x(r)                                    \
     *                                        uncle(b)
     *
     * 2.2 当添加节点是父节点的右子节点：
     *
     *     gx(b)                                gx(b)
     *    /   \              x为添加节点       /  \
     * px(r)  uncle(b)          ==>         x(r)   uncle(b)
     *   \                                 /
     *   x(r)                            px(r)
     *
     *  此时将px看作是新添加的节点就和上面叔叔节点是黑色，添加节点是父节点的左子节点情况，即2.1
     *
     *  当叔叔节点是红色时候
     *  父节点和叔叔节点变为黑色，祖父节点变为红色
     *           gpx(?)                              gpx(?)
     *            /                                   /
     *         gx(b)                              gx(r)
     *        /   \              x为添加节点     /   \
     *     px(r)  uncle(r)          ==>       px(b)  uncle(b)
     *    /                                  /
     *  x(r)                               x(r)
     *
     *对于1 即叔叔节点是红色时候 经过上面调整后，gpx(祖父节点的父节点)如果颜色为红色，此时还要调整，将祖父节点作为新添加节点继续处理
     * @param x
     */
    private void fixAfterAdd(Node<T> x) {
        x.color = RED;
        while (parentOf(x) != null && parentOf(x).color == RED){
           if (parentOf(x) == leftOf(parentOf(parentOf(x)))){//父节点是祖父节点的左子节点
               Node<T> y = rightOf(parentOf(parentOf(x)));//叔叔节点
               if (colorOf(y) == RED){//叔叔节点也是红色
                   setColor(parentOf(x),BLACK);
                   setColor(y,BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   x = parentOf(parentOf(x));
               }else {//叔叔节点是黑色
                   if (x == rightOf(parentOf(x))){//当前节点是父节点的右子节点
                        x=parentOf(x);
                        leftRotation(x);
                   }
                   setColor(parentOf(x),BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   rightRotation(parentOf(parentOf(x)));
               }
           }else {//父节点是祖父节点的右子节点
               Node<T> y = leftOf(parentOf(parentOf(x)));//叔叔节点
               if (colorOf(y) == RED){
                   setColor(y,BLACK);
                   setColor(parentOf(x),BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   x = parentOf(x);
               }else {
                   if (x == leftOf(parentOf(x))){
                       x = parentOf(x);
                       rightRotation(x);
                   }
                   setColor(parentOf(x),BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   leftRotation(parentOf(parentOf(x)));
               }
           }
        }

        setColor(root,BLACK);
    }

    public boolean remove(T key){
        if (root == null){
            return false;
        }else {
            Node<T> current = root;
            int cmp;
            do {
                cmp = key.compareTo(current.key);
                if (cmp < 0){
                    current = current.left;
                }else if (cmp > 0){
                    current = current.right;
                }else break;
            }while (current != null);

            if (current == null){//未找到对应的节点
                return false;
            }else {
                if (leftOf(current) != null && rightOf(current) != null){//当前删除节点左/右子节点不为空
                    Node<T> s = successor(current);//获取当前删除节点的后继节点
                    current.key = s.key;//后继节点的值赋给当前删除节点
                    current = s;//当前删除节点指向后继节点
                }

                /**
                 * 若开始的删除节点左右子节点都不为空，现在的删除节点指向了后继节点
                 * 若开始的删除节点左/右节点至少有一个为空，现在的删除节点未做改变
                 * 下面的replace节点为当前删除节点的左子节点或右子节点
                 */
                Node<T> replace = leftOf(current) != null ? leftOf(current) : rightOf(current);

                if (replace != null){
                    replace.parent = current.parent;
                    if (parentOf(current) == null){//删除节点是根节点，且根节点只有一个子节点
                        root = replace;//将根节点指向原来根节点的那个子节点
                    }else if (current == leftOf(parentOf(current))){//当前删除节点是父节点的左子节点
                        current.parent.left = replace;
                    }else {
                        current.parent.right = replace;
                    }

                    current.left = current.right = current.parent = null;
                    if (colorOf(current) == BLACK){
                        fixAfterRemove(replace);
                    }

                }else if (current.parent == null){//删除节点为根节点 且没有子节点
                    root = null;
                }else {//删除节点没有子节点，但是不为根节点
                    if (current.color == BLACK){
                        fixAfterRemove(current);
                    }

                    if (current.parent != null){
                        if ( current == parentOf(current).left){
                            parentOf(current).left = null;
                        }else {
                            parentOf(current).right = null;
                        }
                        current.parent = null;
                    }
                }


            }
        }
        return true;
    }

    /**
     * 删除结束调整
     *
     * 1.若当前节点颜色为红+黑色，当前节点颜色置为黑色
     *
     * 2. 当前节点颜色为黑+黑色
     * 2.1 当前节点为父节点的左子节点
     *
     *
     *
     * 2.1.1 若兄弟节点为红色，
     *                                     sib(b)
     *                                    /    \
     *    px(b)                        px(r)   sibR(b)
     *   /  \           ==>           /   \                  --->此时变为兄弟节点变为黑色，后续情况讨论
     * x(b+b) sib(r)              x(b+b)  sibL(b)
     *     /    \
     *  sibL(b)  sibR(b)
     *
     * 2.1.2 若兄弟节点为黑色
     *   -1 兄弟节点的左右子节点也为黑色
     *
     *      px(b)              px(b+b)
     *     /   \        ==>    /   \                  --->此时将黑+黑节点转移到原来的父节点
     * x(b+b)  sib(b)        x(b)   sib(r)
     *         /   \               /    \
     *     lsib(b) rsib(b)     lsib(r)  rsib(b)
     *
     *   -2 兄弟节点的的右子节点为黑色,左子节点为红色
     *
     *     px(b)                             px(b)
     *     /   \                             /   \
     * x(b+b)  sib(b)              ==>   x(b+b)  lsib(b)        --->此时变为兄弟节点为黑色，兄弟节点的右子节点为红色
     *         /   \                              \
     *      lsib(r) lsib(b)                      sib(r)
     *                                             \
     *                                            lsib(b)
     *    -3  兄弟节点的右子节点为红色
     *    px(b)                          sib(b)
     *    /   \                         /    \
     * x(b+b) sib(b)            ==>  px(b)   rsib(b)
     *        /   \                  /  \
     *    lsib(?)  rsib(r)         x(b) lsib(?)
     * @param node
     */
    private void fixAfterRemove(Node<T> node) {
        while (node != root && colorOf(node) == BLACK){
            if (node == leftOf(parentOf(node))){//node为其父节点的左子节点
                Node<T> b = rightOf(parentOf(node));//获取node兄弟节点
                if (colorOf(b) == RED){//若兄弟节点为红色
                    setColor(b,BLACK);
                    setColor(parentOf(node),RED);
                    leftRotation(parentOf(node));
                    b = rightOf(parentOf(node));
                }

                if (colorOf(leftOf(b)) == BLACK && colorOf(rightOf(b)) == BLACK){
                    setColor(b,RED);
                    node = parentOf(node);
                }else {
                    if (colorOf(rightOf(node)) == BLACK){
                        setColor(leftOf(b),BLACK);
                        setColor(b,RED);
                        rightRotation(b);
                        b = rightOf(parentOf(node));
                    }
                    setColor(b,colorOf(parentOf(node)));
                    setColor(parentOf(node),BLACK);
                    setColor(rightOf(b),BLACK);
                    leftRotation(parentOf(node));
                    node = root;
                }

            }else {
                Node<T> b = leftOf(parentOf(node));
                if (colorOf(b) == RED){//若兄弟节点为红色
                    setColor(b,BLACK);
                    setColor(parentOf(node),RED);
                    rightRotation(parentOf(node));
                    b = leftOf(parentOf(node));
                }

                if (colorOf(leftOf(b)) == BLACK && colorOf(rightOf(b)) == BLACK){
                    setColor(b,RED);
                    node = parentOf(node);
                }else {
                    if (colorOf(rightOf(node)) == BLACK){
                        setColor(leftOf(b),BLACK);
                        setColor(b,RED);
                        leftRotation(b);
                        b = leftOf(parentOf(node));
                    }
                    setColor(b,colorOf(parentOf(node)));
                    setColor(parentOf(node),BLACK);
                    setColor(rightOf(b),BLACK);
                    rightRotation(parentOf(node));
                    node = root;
                }
            }
        }
        setColor(node,BLACK);
    }

    private Node<T> successor(Node<T> node) {
        Node<T> result = rightOf(node);
        while (result != null && leftOf(result) != null){
            result = leftOf(result);
        }
        return result;
    }


    public static void main(String[] args) {
        RBTree<Integer> tree = new RBTree<>();
        tree.add(1);
        tree.add(2);
        tree.add(3);
        tree.remove(2);
        System.out.println();
    }
}
